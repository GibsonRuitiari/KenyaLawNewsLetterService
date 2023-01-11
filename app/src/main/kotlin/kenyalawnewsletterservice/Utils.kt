package kenyalawnewsletterservice

import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.io.IOException

suspend fun <T> retryIO(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T): T
{
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            DependencyProvider.getLoggerInstance().logMessage("the following error occurred while parsing the page $e..retrying")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}
object DependencyProvider{
    fun getLoggerInstance():Logger = DefaultLogger()
    fun getNewsLetterLinksService():NewsLettersLinksService =NewsLettersLinksServiceDelegate()
    fun getCaseContentService():CaseScraperService = CaseScraperServiceDelegate()
    fun getJsonInstance():Json =  Json {
        prettyPrint=true
        explicitNulls=false
    }
}