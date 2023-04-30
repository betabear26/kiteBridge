import com.zerodhatech.models.Order
import com.zerodhatech.models.Tick
import com.zerodhatech.models.User
import core.kite.KiteAuthenticator
import core.kite.KiteListener
import java.awt.Desktop
import java.net.URI
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    Main().init()
}
class Main: KiteListener {
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
        println("Logged in as ${user.userName}")
    }

    override fun onKiteDisconnected() {
        println("Logged out")
        exitProcess(0)
    }

    override fun onTickerConnected() {
    }

    override fun onTickerDisconnected() {
    }

    override fun onOrderUpdate(it: Order?) {
    }

    override fun onTickerArrival(it: ArrayList<Tick>?) {
    }

}