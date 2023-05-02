import com.google.gson.GsonBuilder
import com.zerodhatech.models.Order
import com.zerodhatech.models.Tick
import com.zerodhatech.models.User
import core.database.RedisDb
import core.engine.manager.InstrumentManager
import core.engine.manager.OrderManager
import core.engine.manager.TickerManager
import core.kite.KiteAuthenticator
import core.kite.KiteListener
import java.awt.Desktop
import java.net.URI
import java.text.SimpleDateFormat
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    Main().init()
}
class Main: KiteListener {

    private val dateFormat = SimpleDateFormat("ddMMMyyyy:HHmmss")
    private var tickerReconnectionCount: Int = 0
    private val instrumentManager: InstrumentManager by lazy {
        InstrumentManager()
    }
    private val tickerManager: TickerManager by lazy {
        TickerManager()
    }
    private val orderManager:OrderManager by lazy {
        OrderManager()
    }
    private val redis by lazy {
        RedisDb()
    }
    private val gson by lazy {
        GsonBuilder()
            .setDateFormat("ddMMMyyyy:HHmmss")
            .create()
    }

    fun init(){
        println("Initializing...")
        val url = KiteAuthenticator.login(this)
        println("Please open the following URL in your browser and authorize the app.")
        println(url)
        try {
            val desktop = Desktop.getDesktop()
            desktop.browse(URI(url))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        print("Enter the request token: ")
        val requestToken = readlnOrNull()
        if(requestToken.isNullOrEmpty()) {
            print("Request token cannot be empty")
            return
        }
        KiteAuthenticator.authenticate(requestToken, this)
        print("DO NOT EXIT THIS PROGRAM. USE THE STOP BUTTON IN YOUR IDE TO STOP THE PROGRAM.")
        Thread.currentThread().join()
    }

    override fun loggingIn() {
        println("Logging in...")
    }

    override fun onKiteConnected(user: User) {
        tickerReconnectionCount = 0
        println("Logged in as ${user.userName}")
        redis.intervalDumpToInfluxDb(60L)
        tickerManager.subscribeAll(this)
    }

    override fun onKiteDisconnected() {
        println("Logged out")
        exitProcess(0)
    }

    override fun onTickerConnected() {
        println("Ticker connected")
    }


    override fun onTickerDisconnected() {
        println("Ticker disconnected")
        if(tickerReconnectionCount < 5){
            println("Reconnecting...")
            tickerReconnectionCount++
            tickerManager.subscribeAll(this)
        }
    }

    override fun onOrderUpdate(it: Order?) {
    }

    override fun onTickerArrival(it: ArrayList<Tick>?) {
        if(it.isNullOrEmpty()){
            println("Empty tick list received")
            return
        }
        it.forEach {
            println("${it.instrumentToken} -> ${it.lastTradedPrice} at ${dateFormat.format(it.tickTimestamp)}")
            it.tickTimestamp = dateFormat.parse(dateFormat.format(it.tickTimestamp))
            tickerManager.writeToRedis(it, gson, redis)
        }
    }

}