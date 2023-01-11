package kenyalawnewsletterservice

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.awaitBody(): ResponseBody {
    return suspendCancellableCoroutine {
        it.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    it.resume(response.body!!)
                } else {
                    it.resumeWithException(IOException("HTTP ${response.code} ${response.message}"))
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(e)
            }
        })
    }
}

fun String.createHtmlDocumentFromUrl(logger: Logger): Document? {
   return try {
        val responseStringBody= toHttpUrl().toUrl().openStream().bufferedReader().use {
           it.readText() }
         Jsoup.parse(responseStringBody)
    }catch (ex:IOException){
        logger.logMessage("the following exception occurred while creating html document ${ex.message}")
        null
    }
}

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Array<*> -> this.toJsonArray()
    is List<*> -> this.toJsonArray()
    is Map<*, *> -> this.toJsonObject()
    else -> JsonNull
}
fun Array<*>.toJsonArray() = JsonArray(map { it.toJsonElement() })
fun Iterable<*>.toJsonArray() = JsonArray(map { it.toJsonElement() })
fun Map<*, *>.toJsonObject() = JsonObject(mapKeys { it.key.toString() }.mapValues { it.value.toJsonElement() })