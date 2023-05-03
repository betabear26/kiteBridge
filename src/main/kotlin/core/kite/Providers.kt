package core.kite

import com.zerodhatech.models.Order
import core.kite.KiteAuthenticator.getKite
import com.zerodhatech.models.OrderParams
import com.zerodhatech.ticker.KiteTicker
import kotlin.random.Random

object Providers {

    object MarginProvider {
        fun getMargin(): String {
            val kiteSdk = getKite()
            val margins = kiteSdk.getMargins("equity")
            return margins.available.cash
        }
    }

    object OrderProvider {
        fun placeOrder(orderParams: OrderParams, variety: String): Order {
            val kiteSdk = getKite()
            return kiteSdk.placeOrder(orderParams, variety)
        }

        fun cancelOrder(orderId: String, variety: String): Order {
            val kiteSdk = getKite()
            return kiteSdk.cancelOrder(variety, orderId)
        }
    }

    object TickerLiveDataProvider {

        private lateinit var tickerProvider: KiteTicker

        fun subscribe(tradingSymbol: ArrayList<Long>, kiteListener: KiteListener) {
            val kiteSdk = getKite()
            tickerProvider = KiteTicker(kiteSdk.accessToken, kiteSdk.apiKey)
            tickerProvider.setOnConnectedListener {
                kiteListener.onTickerConnected()
                tickerProvider.subscribe(tradingSymbol)
                tickerProvider.setMode(tradingSymbol, KiteTicker.modeFull)
            }

            tickerProvider.setOnDisconnectedListener {
                kiteListener.onTickerDisconnected()
            }

            tickerProvider.setOnOrderUpdateListener {
                kiteListener.onOrderUpdate(it)
            }

            tickerProvider.setOnTickerArrivalListener {
                kiteListener.onTickerArrival(it)
            }

            tickerProvider.setTryReconnection(true)
            tickerProvider.setMaximumRetries(10)
            tickerProvider.setMaximumRetryInterval(30)
            tickerProvider.connect()
        }

        fun unsubscribe(tradingSymbol: ArrayList<Long>) {
            tickerProvider.unsubscribe(tradingSymbol)
            tickerProvider.disconnect()
        }

    }

    object TokenProvider {

        fun getInstrumentTokens(tradingSymbols: List<String>): List<Long> {
            println("Requesting tokens for $tradingSymbols")
            val kiteSdk = getKite()
            val instruments = kiteSdk.instruments

            return tradingSymbols.map { symbol ->
                instruments.find { it.tradingsymbol == symbol }?.instrument_token ?: 0
            }
        }


    }


}