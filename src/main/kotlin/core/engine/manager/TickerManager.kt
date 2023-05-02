package core.engine.manager

import com.google.gson.Gson
import com.zerodhatech.models.Tick
import core.database.RedisDb
import core.kite.KiteListener
import core.kite.Providers
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TickerManager {

    companion object {
        const val NIFTY = "nifty"
        const val BANK_NIFTY = "banknifty"
    }

    val tickerMap = mutableMapOf(
        Pair(NIFTY, "NIFTY"),
        Pair(BANK_NIFTY, "BANKNIFTY")
    )

    val nextMonthName by lazy {
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
        tokenMap[tokens[0]] = NIFTY
        tokenMap[tokens[1]] = BANK_NIFTY
    }

    fun subscribeAll(listener: KiteListener){
        Providers.TickerLiveDataProvider.subscribe(
            ArrayList(tokenMap.keys.toList()),
            listener
        )
    }

    fun writeToRedis(tickers: List<Tick>, gson: Gson, redisDb: RedisDb){
        tickers.forEach {
            val key = "${tokenMap[it.instrumentToken]}:${it.tickTimestamp}"
            val value = gson.toJson(it)
            redisDb.set(key, value)
            println("Writing to redis: ${tokenMap[it.instrumentToken]} -> ${it.lastTradedPrice}")
        }
    }

}