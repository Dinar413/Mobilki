package com.dinar.myproject.data.currency

import com.dinar.myproject.data.ServiceLocator
import kotlinx.coroutines.flow.first
import org.json.JSONObject

object CurrencyConverter {

    suspend fun fromRub(amountRub: Double, targetCurrency: String): Double {
        if (targetCurrency == "RUB") return amountRub

        val ratesJson = ServiceLocator.session.ratesJsonFlow.first()
        if (ratesJson.isBlank()) return amountRub

        val rate = extractRate(ratesJson, targetCurrency) ?: return amountRub
        return amountRub * rate
    }

    private fun extractRate(json: String, currency: String): Double? {
        return try {
            val root = JSONObject(json)
            val rates = root.getJSONObject("rates")
            rates.getDouble(currency)
        } catch (e: Exception) {
            null
        }
    }

    fun symbol(code: String): String = when (code) {
        "USD" -> "$"
        "EUR" -> "€"
        "TRY" -> "₺"
        else -> "₽"
    }
}
