package core.util

import java.io.File

object Logger {

    val currentPath = File(".").absolutePath
    val logFile = File("$currentPath/log.txt")
    val created = if(logFile.exists()) true else logFile.createNewFile()

    fun log(message: String){
        if(created) logFile.appendText("$message\n")
    }



}