package com.dinar.myproject.data.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Rates(
    val base: String,
    val timeLastUpdateUnix: Long,
    val map: Map<String, Double>
)

object ExchangeRatesApi {

    private const val ENDPOINT = "https://open.er-api.com/v6/latest/RUB"

    fun fetchRates(): Rates {
        val url = URL(ENDPOINT)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(body)

        val result = root.optString("result", "error")
        if (result != "success") {
            val errorType = root.optString("error-type", "unknown")
            throw IllegalStateException("API error: $errorType")
        }

        val base = root.getString("base_code") // "RUB"
        val timeUnix = root.optLong("time_last_update_unix", 0L)

        val ratesObj = root.getJSONObject("rates")
        val keys = ratesObj.keys()

        val map = mutableMapOf<String, Double>()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = ratesObj.getDouble(k)
        }

        return Rates(base = base, timeLastUpdateUnix = timeUnix, map = map)
    }
}
