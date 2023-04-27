package core.kite

import com.zerodhatech.kiteconnect.KiteConnect
import core.util.EnvProvider
import io.github.cdimascio.dotenv.Dotenv

object KiteAuthenticator {

    private val apiKey by lazy {
        EnvProvider.getEnvVar("API_KEY")
    }

    private val userId by lazy {
        EnvProvider.getEnvVar("USER_ID")
    }

    private val apiSecret by lazy {
        EnvProvider.getEnvVar("API_SECRET")
    }

    private val accessToken by lazy {
        "your_access_token"
    }

    private val kiteConnect: KiteConnect by lazy {
        KiteConnect(apiKey)
    }

    fun getKiteConnect(): KiteConnect = kiteConnect

    fun login(kiteListener: KiteListener): String = run {
        kiteListener.loggingIn()
        kiteConnect.userId = userId
        kiteConnect.loginURL
    }

    fun authenticate(requestToken: String, kiteListener: KiteListener){
        val user = kiteConnect.generateSession(requestToken, apiSecret)
        kiteConnect.accessToken = user.accessToken
        kiteConnect.publicToken = user.publicToken
        kiteListener.onKiteConnected(user)
        kiteConnect.setSessionExpiryHook {
            kiteListener.onKiteDisconnected()
        }
    }


}