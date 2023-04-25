package core.kite

import com.zerodhatech.kiteconnect.KiteConnect

object KiteAuthenticator {

    private val apiKey by lazy {
        "your_api_key"
    }

    private val userId by lazy {
        "your_user_id"
    }

    private val apiSecret by lazy {
        "your_api_secret"
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