package core.database

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import core.util.EnvProvider
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RedisDb {

    @OptIn(DelicateCoroutinesApi::class)
    private val coroutineContext = newSingleThreadContext("RedisToInfluxDb")

    private val host = EnvProvider.getEnvVar("REDIS_HOST")
    private val port = EnvProvider.getEnvVar("REDIS_PORT").toInt()

    private val redisClient by lazy {
        RedisClient.create(RedisURI.create(host, port))
    }

    private lateinit var connection: StatefulRedisConnection<String, String>
    private lateinit var commands: RedisCommands<String, String>


    private val influxDbDatabase = EnvProvider.getEnvVar("INFLUXDB_DATABASE")

    private val influxDb: InfluxDBClientKotlin by lazy {
        InfluxDBClientKotlinFactory.create(
            EnvProvider.getEnvVar("INFLUXDB_URL"),
            EnvProvider.getEnvVar("INFLUXDB_TOKEN").toCharArray(),
            EnvProvider.getEnvVar("INFLUXDB_ORG")
        )
    }

    init {
        println("Connecting to Redis...")
        connect()
    }

    private fun connect() {
        connection = redisClient.connect()
        commands = connection.sync()
    }

    fun disconnect() {
        connection.close()
        redisClient.shutdown()
    }

    fun set(key: String, value: String) {
        commands.set(key, value)
    }

    fun get(key: String): String? {
        return commands.get(key)
    }

    fun del(key: String) {
        commands.del(key)
    }

    fun intervalDumpToInfluxDb(interval: Long) {
        val scheduler = Executors.newScheduledThreadPool(1)

        scheduler.scheduleAtFixedRate({
            println("Dumping Redis to InfluxDB...")
            val keys = commands.keys("*")
            for (key in keys) {
                val value = commands.get(key)
                val point = Point.measurement(key)
                    .addTag("key", key)
                    .addField("value", value)
                    .time(System.currentTimeMillis(), WritePrecision.MS)
                runBlocking(coroutineContext) {
                    influxDb.getWriteKotlinApi().writePoint(point, influxDbDatabase)
                }
            }
        }, 60, interval, TimeUnit.MINUTES)
    }
}