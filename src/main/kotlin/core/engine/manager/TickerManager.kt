package core.engine.manager

import com.google.gson.Gson
import com.zerodhatech.models.Tick
import core.database.RedisDb
import core.kite.KiteListener
import core.kite.Providers
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import java.util.SimpleTimeZone

class TickerManager {

    companion object {
        const val NIFTY = "nifty"
        const val BANK_NIFTY = "banknifty"
    }

    private val tickerMap = mutableMapOf(
        Pair(NIFTY, "NIFTY 50"),
        Pair(BANK_NIFTY, "NIFTY BANK")
    )

    val nextMonthName: String by lazy {
        LocalDate.now().month.plus(1).getDisplayName(
            TextStyle.SHORT,
            Locale.ENGLISH
        )
    }

    private var tokenMap = mutableMapOf<Long, String>()

    init {
        getInstrumentTokens()
    }

    private fun getInstrumentTokens() {
        val tokens = Providers.TokenProvider.getInstrumentTokens(
            tickerMap.values.toList()
        )
        println("Received tokens - > $tokens}")
        tokenMap[tokens[0]] = NIFTY
        tokenMap[tokens[1]] = BANK_NIFTY
    }

    fun subscribeAll(listener: KiteListener) {
        println("Subscribin to ${tokenMap.keys.toList()}}")
        Providers.TickerLiveDataProvider.subscribe(
            ArrayList(tokenMap.keys.toList()),
            listener
        )
    }

    fun writeToRedis(tickers: List<Tick>, gson: Gson, redisDb: RedisDb, dateFormat: SimpleDateFormat) {
        tickers.forEach {
            val tickTimestamp = dateFormat.parse(dateFormat.format(it.tickTimestamp))
            val key = "${tokenMap[it.instrumentToken]}:${tickTimestamp}"
            val value = gson.toJson(it)
            redisDb.set(key, value)
            println("Writing to redis: ${tokenMap[it.instrumentToken]} -> ${it.lastTradedPrice}")
        }
    }

    fun writeToRedis(ticker: Tick, gson: Gson, redisDb: RedisDb, dateFormat: SimpleDateFormat) {
        val tickTimestamp = dateFormat.parse(dateFormat.format(ticker.tickTimestamp))
        val key = "${tokenMap[ticker.instrumentToken]}:${tickTimestamp}"
        val value = gson.toJson(ticker)
        redisDb.set(key, value)
        println("Writing to redis: ${tokenMap[ticker.instrumentToken]} -> ${ticker.lastTradedPrice}")
    }

}