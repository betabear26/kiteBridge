package core.engine.manager

import com.google.gson.Gson
import com.zerodhatech.models.Tick
import core.database.RedisDb

class TickerManager {

    fun writeToRedis(key: String,
                     ticker: Tick,
                     gson: Gson,
                     redisDb: RedisDb
    ) {
        val value = gson.toJson(ticker)
        redisDb.set(key, value)
        println("Writing to redis: $key -> ${ticker.lastTradedPrice}")
    }

}