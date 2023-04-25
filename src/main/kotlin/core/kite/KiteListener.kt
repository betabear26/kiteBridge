package core.kite

import com.zerodhatech.models.Order
import com.zerodhatech.models.Tick
import com.zerodhatech.models.User
import java.util.ArrayList

interface KiteListener {

    fun loggingIn()
    fun onKiteConnected(user: User)
    fun onKiteDisconnected()


    fun onTickerConnected()
    fun onTickerDisconnected()
    fun onOrderUpdate(it: Order?)
    fun onTickerArrival(it: ArrayList<Tick>?)
}