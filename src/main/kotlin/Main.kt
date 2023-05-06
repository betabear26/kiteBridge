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
        TickerManager(instrumentManager)
    }
    private val orderManager:OrderManager by lazy {
        OrderManager(instrumentManager, tickerManager)
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
        println("DO NOT EXIT THIS PROGRAM.")
        Thread.currentThread().join()
    }

    override fun loggingIn() {
        println("Logging in...")
    }

    override fun onKiteConnected(user: User) {
        tickerReconnectionCount = 0
        println("Logged in as ${user.userName}")
        redis.intervalDumpToInfluxDb(20L)
        instrumentManager.subscribeUnderlying(this)
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
            instrumentManager.subscribeUnderlying(this)
        }
    }

    override fun onOrderUpdate(it: Order?) {
        if(it == null){
            println("Empty order update received")
            return
        }
        println("${it.orderId} for ${it.tradingSymbol} -> ${it.status}")
        //orderManager.onOrderUpdate(it)
    }

    override fun onTickerArrival(it: ArrayList<Tick>?) {
        if(it.isNullOrEmpty()){
            //println("Empty tick list received")
            return
        }
        orderManager.onTick(it)
        it.forEach {
            val name = instrumentManager.getInstrumentName(it.instrumentToken)
            //println("$name -> ${it.lastTradedPrice} at ${dateFormat.format(it.tickTimestamp)}")
            val key = "$name:${dateFormat.format(it.tickTimestamp)}"
            tickerManager.saveData(key, it, gson, redis)
        }
    }

}