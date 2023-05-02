package core.engine.manager

import com.zerodhatech.models.Order
import com.zerodhatech.models.OrderParams
import com.zerodhatech.models.Tick
import core.engine.manager.InstrumentManager.Companion.NIFTY
import core.kite.Providers
import core.util.EnvProvider
import java.util.ArrayList

class OrderManager(
    private val instrumentManager: InstrumentManager
) {

    private val orderEndList = listOf<String>(
        "CANCELLED",
        "COMPLETE",
        "REJECTED"
    )

    private val niftyEnabled = EnvProvider.getEnvVar("NIFTY_ENABLED").toBoolean()
    private var niftyOrder: Order? = null
    private var inNiftyTrade: Boolean = false
    private val niftyLast60Val: MutableList<Double> = mutableListOf()
    private var niftyMin = 0.0
    private var niftyMax = 0.0

    fun onTick(it: ArrayList<Tick>) {
        val tick = it[0]
        val instrument = instrumentManager.underlyingTokenMap[tick.instrumentToken]
        if (instrument == NIFTY) {
            tradeNifty(tick)
        } else tradeBankNifty(tick)
    }

    /**** --------------- NIFTY ------------------ ****/
    private fun tradeNifty(tick: Tick) {
        if(!niftyEnabled)return
        if(!inNiftyTrade) {
            if (tick.lastTradedPrice < niftyMin) {
                placeNiftyPutOrder(tick.lastTradedPrice)
            } else if (tick.lastTradedPrice > niftyMax) {
                placeNiftyCallOrder(tick.lastTradedPrice)
            }
        } else{
            if(niftyOrder != null && niftyOrder!!.status in orderEndList ) {
                inNiftyTrade = false
                niftyOrder = null
            }
        }

        niftyLast60Val.add(tick.lastTradedPrice)

        if (niftyLast60Val.size > 60) {
            niftyLast60Val.removeAt(0)
        }

        niftyMin = niftyLast60Val.min()
        niftyMax = niftyLast60Val.max()
    }

    private fun placeNiftyPutOrder(ltp: Double){
        val strike = (ltp/100).toInt() * 100
        val putInstrument = "NIFTY${instrumentManager.NEXT_EXPIRY}${strike}PE"
        val orderParams = OrderParams()
        orderParams.exchange = "NFO"
        orderParams.tradingsymbol = putInstrument
        orderParams.transactionType = "BUY"
        orderParams.quantity = 75 // TODO
        orderParams.orderType = "MARKET"
        orderParams.product = "NRML"
        orderParams.validity = "IOC"
        orderParams.squareoff = 100.00 // TODO,
        orderParams.stoploss = 100.00 // TODO,
        orderParams.trailingStoploss = 100.00// TODO,
        orderParams.tag = "Nitfy Put Order $ltp"

        niftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
    }

    private fun placeNiftyCallOrder(ltp: Double){
        val strike = ((ltp/100) + 1).toInt() * 100
        val callInstrument = "NIFTY${instrumentManager.NEXT_EXPIRY}${strike}CE"
        val orderParams = OrderParams()
        orderParams.exchange = "NFO"
        orderParams.tradingsymbol = callInstrument
        orderParams.transactionType = "BUY"
        orderParams.quantity = 75 // TODO
        orderParams.orderType = "MARKET"
        orderParams.product = "NRML"
        orderParams.validity = "IOC"
        orderParams.squareoff = 100.00 // TODO,
        orderParams.stoploss = 100.00 // TODO,
        orderParams.trailingStoploss = 100.00// TODO,
        orderParams.tag = "Nitfy Call Order $ltp"

        niftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
    }


    /**** --------------- BANK NIFTY ------------------ ****/
    private val bankniftyEnabled = EnvProvider.getEnvVar("BANKNIFTY_ENABLED").toBoolean()
    private var bankniftyOrder: Order? = null
    private var inBankNiftyTrade: Boolean = false
    private val bankniftyLast60Val: MutableList<Double> = mutableListOf()
    private var bankniftyMin = 0.0
    private var bankniftyMax = 0.0
    private fun tradeBankNifty(tick: Tick) {
        if(!bankniftyEnabled)return
        if(!inBankNiftyTrade) {
            if (tick.lastTradedPrice < bankniftyMin) {
                placeBankNiftyPutOrder(tick.lastTradedPrice)
            } else if (tick.lastTradedPrice > bankniftyMax) {
                placeBankNiftyCallOrder(tick.lastTradedPrice)
            }
        } else{
            if(bankniftyOrder != null && bankniftyOrder!!.status in orderEndList ) {
                inBankNiftyTrade = false
                bankniftyOrder = null
            }
        }

        bankniftyLast60Val.add(tick.lastTradedPrice)

        if (bankniftyLast60Val.size > 60) {
            bankniftyLast60Val.removeAt(0)
        }

        bankniftyMin = bankniftyLast60Val.min()
        bankniftyMax = bankniftyLast60Val.max()

    }

    private fun placeBankNiftyPutOrder(ltp: Double){
        val strike = (ltp/100).toInt() * 100
        val putInstrument = "BANKNIFTY${instrumentManager.NEXT_EXPIRY}${strike}PE"

        val orderParams = OrderParams()
        orderParams.exchange = "NFO"
        orderParams.tradingsymbol = putInstrument
        orderParams.transactionType = "BUY"
        orderParams.quantity = 75 // TODO
        orderParams.orderType = "MARKET"
        orderParams.product = "NRML"
        orderParams.validity = "IOC"
        orderParams.squareoff = 100.00 // TODO,
        orderParams.stoploss = 100.00 // TODO,
        orderParams.trailingStoploss = 100.00// TODO,
        orderParams.tag = "Bank Nitfy Put Order $ltp"

        niftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
    }

    private fun placeBankNiftyCallOrder(ltp: Double){
        val strike = ((ltp/100) + 1).toInt() * 100
        val callInstrument = "BANKNIFTY${instrumentManager.NEXT_EXPIRY}${strike}CE"

        val orderParams = OrderParams()
        orderParams.exchange = "NFO"
        orderParams.tradingsymbol = callInstrument
        orderParams.transactionType = "BUY"
        orderParams.quantity = 75 // TODO
        orderParams.orderType = "MARKET"
        orderParams.product = "NRML"
        orderParams.validity = "IOC"
        orderParams.squareoff = 100.00 // TODO,
        orderParams.stoploss = 100.00 // TODO,
        orderParams.trailingStoploss = 100.00// TODO,
        orderParams.tag = "Bank Nitfy call Order $ltp"
        niftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
    }

}