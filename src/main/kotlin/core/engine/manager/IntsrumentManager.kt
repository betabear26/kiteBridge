package core.engine.manager

import core.kite.KiteListener
import core.kite.Providers
import core.util.EnvProvider
import kotlin.collections.ArrayList

class InstrumentManager {

    companion object {
        const val NIFTY = "nifty"
        const val BANK_NIFTY = "banknifty"

        const val PREFIX_NIFTY = "NIFTY"
        const val PREFIX_BANKNIFTY = "BANKNIFTY"

        const val SUFFIX_CALL = "CE"
        const val SUFFIX_PUT = "PE"
    }

    val NEXT_EXPIRY= EnvProvider.getEnvVar("NEXT_EXPIRY")


    private val tickerMap = mutableMapOf(
        Pair(NIFTY, "NIFTY 50"),
        Pair(BANK_NIFTY, "NIFTY BANK")
    )
    var underlyingTokenMap = mutableMapOf<Long, String>()

    // nifty option tokens
    var niftyCallTokens : Map<String, Long> = mutableMapOf()
    var niftyPutTokens : Map<String, Long> = mutableMapOf()

    // banknifty option tokens
    var bankniftyCallTokens : Map<String, Long> = mutableMapOf()
    var bankniftyPutTokens : Map<String, Long> = mutableMapOf()

    init {
        getInstrumentTokens()
    }

    private fun getInstrumentTokens() {
        val tokens = Providers.TokenProvider.getInstrumentTokens(
            tickerMap.values.toList()
        )
        println("Received tokens - > $tokens}")
        underlyingTokenMap[tokens[0]] = NIFTY
        underlyingTokenMap[tokens[1]] = BANK_NIFTY

        getNiftyOptionTokens(EnvProvider.getEnvVar("NIFTY_LTP").toDouble())
        getBankNiftyOptionTokens(EnvProvider.getEnvVar("BANKNIFTY_LTP").toDouble())
    }

    private fun getNiftyOptionTokens(ltp: Double){
        val strike = (ltp / 100).toInt() * 100

        val putTokens = mutableListOf<Pair<String, Long>>()
        val callTokens = mutableListOf<Pair<String, Long>>()

        val instrumentList = mutableListOf<String>()

        for (i in -5..5) {
            val putStrike = strike - (i * 100)
            val callStrike = strike + (i * 100)

            val putInstrument = "$PREFIX_NIFTY$NEXT_EXPIRY${putStrike}PE"
            val callInstrument = "$PREFIX_NIFTY$NEXT_EXPIRY${callStrike}CE"

            instrumentList.add(putInstrument)
            instrumentList.add(callInstrument)
        }

        val tokens = Providers.TokenProvider.getInstrumentTokens(instrumentList)

        for (i in tokens.indices step 2) {
            val putToken = tokens[i]
            val callToken = tokens[i + 1]

            val putInstrument = instrumentList[i]
            val callInstrument = instrumentList[i + 1]

            putTokens.add(Pair(putInstrument, putToken))
            callTokens.add(Pair(callInstrument, callToken))
        }

        niftyPutTokens = putTokens.toMap()
        niftyCallTokens = callTokens.toMap()
    }

    private fun getBankNiftyOptionTokens(ltp: Double){
        val strike = (ltp / 100).toInt() * 100

        val putTokens = mutableListOf<Pair<String, Long>>()
        val callTokens = mutableListOf<Pair<String, Long>>()

        val instrumentList = mutableListOf<String>()

        for (i in -5..5) {
            val putStrike = strike - (i * 100)
            val callStrike = strike + (i * 100)

            val putInstrument = "$PREFIX_BANKNIFTY$NEXT_EXPIRY${putStrike}PE"
            val callInstrument = "$PREFIX_BANKNIFTY$NEXT_EXPIRY${callStrike}CE"

            instrumentList.add(putInstrument)
            instrumentList.add(callInstrument)
        }

        val tokens = Providers.TokenProvider.getInstrumentTokens(instrumentList)

        for (i in tokens.indices step 2) {
            val putToken = tokens[i]
            val callToken = tokens[i + 1]

            val putInstrument = instrumentList[i]
            val callInstrument = instrumentList[i + 1]

            putTokens.add(Pair(putInstrument, putToken))
            callTokens.add(Pair(callInstrument, callToken))
        }

        bankniftyPutTokens = putTokens.toMap()
        bankniftyCallTokens = callTokens.toMap()
    }

    fun subscribeUnderlying(listener: KiteListener) {
        println("Subscribing to ${underlyingTokenMap.keys.toList()}}")
        Providers.TickerLiveDataProvider.subscribe(
            ArrayList(underlyingTokenMap.keys.toList()),
            listener
        )

        subscribeNiftyOptions(listener)
        subscribeBankNiftyOptions(listener)
    }

    private fun subscribeNiftyOptions(listener: KiteListener) {
        println("Subscribing to ${niftyCallTokens.values.toList() + niftyPutTokens.values.toList()}")
        Providers.TickerLiveDataProvider.subscribe(
            ArrayList(niftyCallTokens.values.toList() + niftyPutTokens.values.toList()),
            listener
        )
    }

    private fun subscribeBankNiftyOptions(listener: KiteListener) {
        println("Subscribing to ${bankniftyCallTokens.values.toList() + bankniftyPutTokens.values.toList()}")
        Providers.TickerLiveDataProvider.subscribe(
            ArrayList(bankniftyCallTokens.values.toList() + bankniftyPutTokens.values.toList()),
            listener
        )
    }

    fun getInstrumentName(token: Long): String {
        if(underlyingTokenMap.containsKey(token)) {
            return underlyingTokenMap[token]!!
        }

        if(niftyCallTokens.containsValue(token)) {
            return niftyCallTokens.filterValues { it == token }.keys.first()
        }

        if(niftyPutTokens.containsValue(token)) {
            return niftyPutTokens.filterValues { it == token }.keys.first()
        }

        if(bankniftyCallTokens.containsValue(token)) {
            return bankniftyCallTokens.filterValues { it == token }.keys.first()
        }

        if(bankniftyPutTokens.containsValue(token)) {
            return bankniftyPutTokens.filterValues { it == token }.keys.first()
        }

        return "UNKNOWN"
    }
}