package core.engine.manager

import com.google.gson.Gson
import com.zerodhatech.models.Tick
import core.database.RedisDb

class TickerManager(
    private val instrumentManager: InstrumentManager
) {

    private val optionDataMap: MutableMap<Long, MutableList<Double>> = mutableMapOf()

    fun updateOptionsData(tick: Tick) {
        val isOptionData = instrumentManager.niftyCallTokens.containsValue(tick.instrumentToken) ||
            instrumentManager.niftyPutTokens.containsValue(tick.instrumentToken) ||
            instrumentManager.bankniftyCallTokens.containsValue(tick.instrumentToken) ||
            instrumentManager.bankniftyPutTokens.containsValue(tick.instrumentToken)

        if(!isOptionData) return

        optionDataMap.getOrPut(tick.instrumentToken) { mutableListOf() }.add(tick.lastTradedPrice)

        if (optionDataMap[tick.instrumentToken]!!.size > 60) {
            optionDataMap[tick.instrumentToken]!!.removeAt(0)
        }
    }

    fun saveData(key: String,
                 ticker: Tick,
                 gson: Gson,
                 redisDb: RedisDb
    ) {
        val value = gson.toJson(ticker)
        redisDb.set(key, value)
        println("Writing to redis: $key -> ${ticker.lastTradedPrice}")
        updateOptionsData(ticker)
    }

}