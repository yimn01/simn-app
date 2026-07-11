package com.quacko.billing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Tiny HTTP client over HttpURLConnection so the app pulls in no networking libraries.
 * Talks to api.php with an `action` parameter, matching the flat-PHP portal convention.
 *
 * Expected shape of api.php responses (adjust in Repository once the real contract is set):
 *   { "ok": true, "data": <object|array> }   on success
 *   { "ok": false, "error": "message" }        on failure
 */
class ApiClient {

    suspend fun get(action: String, params: Map<String, String?> = emptyMap()): Any =
        withContext(Dispatchers.IO) {
            val query = buildQuery(mapOf("action" to action) + params)
            val url = URL(Config.apiEndpoint() + "?" + query)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("Accept", "application/json")
            }
            readJson(conn)
        }

    suspend fun post(action: String, body: Map<String, String?> = emptyMap()): Any =
        withContext(Dispatchers.IO) {
            val url = URL(Config.apiEndpoint())
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
            }
            val payload = buildQuery(mapOf("action" to action) + body)
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            readJson(conn)
        }

    private fun readJson(conn: HttpURLConnection): Any {
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { r -> r.readText() }
            } ?: ""
            if (code !in 200..299) throw ApiException("HTTP $code: ${text.take(200)}")
            val trimmed = text.trim()
            return when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("{") -> JSONObject(trimmed)
                else -> throw ApiException("Unexpected response: ${trimmed.take(200)}")
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun buildQuery(params: Map<String, String?>): String =
        params.entries
            .filter { it.value != null }
            .joinToString("&") { (k, v) ->
                URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
            }
}

class ApiException(message: String) : Exception(message)
