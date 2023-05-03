package core.engine.manager

import com.zerodhatech.kiteconnect.utils.Constants
import com.zerodhatech.models.Order
import com.zerodhatech.models.OrderParams
import com.zerodhatech.models.Tick
import core.engine.manager.InstrumentManager.Companion.NIFTY
import core.kite.Providers
import core.util.EnvProvider
import java.util.ArrayList

class OrderManager(
    private val instrumentManager: InstrumentManager,
    private val tickerManager: TickerManager
) {

    private val orderEndList = listOf(
        "CANCELLED",
        "COMPLETE",
        "REJECTED"
    )

    fun onTick(it: ArrayList<Tick>) {
        val tick = it[0]
        val instrument = instrumentManager.underlyingTokenMap[tick.instrumentToken]
        if (instrument == NIFTY) {
            tradeNifty(tick)
        } else tradeBankNifty(tick)
    }

    private fun cancelOrder(order: Order){
        Providers.OrderProvider.cancelOrder(order.orderId, order.orderVariety)
    }

    fun onOrderUpdate(order: Order){
        if(order.tradingSymbol.contains("NIFTY")){
            niftyOrderPrice = order.averagePrice.toDouble()
        } else{
            bankniftyOrderPrice = order.averagePrice.toDouble()
        }
    }

    /**** --------------- NIFTY ------------------ ****/
    private val niftyEnabled = EnvProvider.getEnvVar("NIFTY_ENABLED").toBoolean()
    private var niftyOrder: Order? = null
    private var inNiftyTrade: Boolean = false
    private var niftyTradedToken: Long = 0L
    private var niftyOrderPrice: Double = 0.0 // TODO


    private val niftyLast60Val: MutableList<Double> = mutableListOf()
    private var niftyMin = 0.0
    private var niftyMax = 0.0

    private fun tradeNifty(tick: Tick) {
        if(!niftyEnabled)return
        if(!inNiftyTrade) {
            if (tick.lastTradedPrice < niftyMin) {
                placeNiftyPutOrder(tick.lastTradedPrice)
            } else if (tick.lastTradedPrice > niftyMax) {
                placeNiftyCallOrder(tick.lastTradedPrice)
            }
        } else{
            if(niftyOrder != null) {
                val lastPrice: Double = tickerManager.optionDataMap[niftyTradedToken]?.last()!!
                if (lastPrice > niftyOrderPrice + 1.7 || lastPrice < niftyOrderPrice - 1) {
                    cancelOrder(niftyOrder!!)
                    inNiftyTrade = false
                    niftyTradedToken = 0
                }
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
        val instrumentToken = instrumentManager.niftyPutTokens[putInstrument] ?: return
        val lastPrice = tickerManager.optionDataMap[instrumentToken]?.last() ?: return

        val orderParams = OrderParams()
        orderParams.exchange = Constants.EXCHANGE_NFO
        orderParams.tradingsymbol = putInstrument
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY
        orderParams.quantity = 200
        orderParams.orderType = Constants.ORDER_TYPE_MARKET
        orderParams.product = Constants.PRODUCT_NRML
        orderParams.validity = Constants.VALIDITY_IOC
        orderParams.tag = "Nitfy Put Order $lastPrice"
        niftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
        inNiftyTrade = true
        niftyTradedToken = instrumentToken
    }

    private fun placeNiftyCallOrder(ltp: Double){
        val strike = ((ltp/100) + 1).toInt() * 100
        val callInstrument = "NIFTY${instrumentManager.NEXT_EXPIRY}${strike}CE"
        val instrumentToken = instrumentManager.niftyPutTokens[callInstrument] ?: return
        val lastPrice = tickerManager.optionDataMap[instrumentToken]?.last() ?: return


        val orderParams = OrderParams()
        orderParams.exchange = Constants.EXCHANGE_NFO
        orderParams.tradingsymbol = callInstrument
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY
        orderParams.quantity = 200
        orderParams.orderType = Constants.ORDER_TYPE_MARKET
        orderParams.product = Constants.PRODUCT_NRML
        orderParams.validity = Constants.VALIDITY_IOC
        orderParams.tag = "Nitfy Call Order $lastPrice"
        niftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
        inNiftyTrade = true
        niftyTradedToken = instrumentToken
    }


    /**** --------------- BANK NIFTY ------------------ ****/
    private val bankniftyEnabled = EnvProvider.getEnvVar("BANKNIFTY_ENABLED").toBoolean()
    private var bankniftyOrder: Order? = null
    private var inBankNiftyTrade: Boolean = false
    private var bankniftyTradedToken: Long = 0L
    private var bankniftyOrderPrice: Double = 0.0 // TODO

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
            if(bankniftyOrder != null) {
                val lastPrice: Double = tickerManager.optionDataMap[bankniftyTradedToken]?.last()!!
                if (lastPrice > bankniftyOrderPrice + 1.7 || lastPrice < bankniftyOrderPrice - 1) {
                    cancelOrder(bankniftyOrder!!)
                    inBankNiftyTrade = false
                    bankniftyTradedToken = 0
                }
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
        val instrumentToken = instrumentManager.bankniftyPutTokens[putInstrument] ?: return
        val lastPrice = tickerManager.optionDataMap[instrumentToken]?.last() ?: return

        val orderParams = OrderParams()
        orderParams.exchange = Constants.EXCHANGE_NFO
        orderParams.tradingsymbol = putInstrument
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY
        orderParams.quantity = 200
        orderParams.orderType = Constants.ORDER_TYPE_MARKET
        orderParams.product = Constants.PRODUCT_NRML
        orderParams.validity = Constants.VALIDITY_IOC
        orderParams.tag = "Bank Nitfy Put Order $ltp"
        bankniftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
        inBankNiftyTrade = true
        bankniftyTradedToken = instrumentToken
    }

    private fun placeBankNiftyCallOrder(ltp: Double){
        val strike = ((ltp/100) + 1).toInt() * 100
        val callInstrument = "BANKNIFTY${instrumentManager.NEXT_EXPIRY}${strike}CE"
        val instrumentToken = instrumentManager.bankniftyPutTokens[callInstrument] ?: return
        val lastPrice = tickerManager.optionDataMap[instrumentToken]?.last() ?: return

        val orderParams = OrderParams()
        orderParams.exchange = Constants.EXCHANGE_NFO
        orderParams.tradingsymbol = callInstrument
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY
        orderParams.quantity = 200
        orderParams.orderType = Constants.ORDER_TYPE_MARKET
        orderParams.product = Constants.PRODUCT_NRML
        orderParams.validity = Constants.VALIDITY_IOC
        orderParams.tag = "Bank Nitfy call Order $ltp"
        bankniftyOrder = Providers.OrderProvider.placeOrder(orderParams, "regular")
        inBankNiftyTrade = true
        bankniftyTradedToken = instrumentToken
    }

}