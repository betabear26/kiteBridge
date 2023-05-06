package core.util

import java.text.SimpleDateFormat
import java.util.Calendar

object Utility {

    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("ddMMMyyyy:HHmmss")
        return formatter.format(Calendar.getInstance())
    }
}