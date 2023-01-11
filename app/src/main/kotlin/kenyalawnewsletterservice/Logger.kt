package kenyalawnewsletterservice

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface Logger {
    fun logMessage(msg:String)
}

class DefaultLogger:Logger{
    override fun logMessage(msg: String) {
        val time=LocalDateTime.now()
        val stringfiedTime=DateTimeFormatter.ofPattern("yy/MM/dd HH:mm").format(time)
        val logMessage = buildString {
            appendLine("[$stringfiedTime]")
            append(msg)
        }
        println(logMessage)
    }
}