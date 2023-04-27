package core.util

import io.github.cdimascio.dotenv.Dotenv

object EnvProvider {

    private val dotEnv by lazy {
        Dotenv.load()
    }

    public fun getEnvVar(name: String): String = dotEnv[name]

}