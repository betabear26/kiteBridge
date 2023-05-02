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
    private var niftyCallToken = 0L
    private var niftyPutToken = 0L

    // banknifty option tokens
    private var bankniftyCallToken = 0L
    private var bankniftyPutToken = 0L

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
    }

    fun getInstrumentToken(instrument: String) {
        val token = Providers.TokenProvider.getInstrumentTokens(
            listOf(instrument)
        )

        with(instrument){
            when{
                startsWith(PREFIX_NIFTY) && endsWith(SUFFIX_CALL) -> niftyCallToken = token[0]
                startsWith(PREFIX_NIFTY) && endsWith(SUFFIX_PUT) -> niftyPutToken = token[0]
                startsWith(PREFIX_BANKNIFTY) && endsWith(SUFFIX_CALL) -> bankniftyCallToken = token[0]
                startsWith(PREFIX_BANKNIFTY) && endsWith(SUFFIX_PUT) -> bankniftyPutToken = token[0]
            }
        }
    }

    fun subscribeAll(listener: KiteListener) {
        println("Subscribing to ${underlyingTokenMap.keys.toList()}}")
        Providers.TickerLiveDataProvider.subscribe(
            ArrayList(underlyingTokenMap.keys.toList()),
            listener
        )
    }
}