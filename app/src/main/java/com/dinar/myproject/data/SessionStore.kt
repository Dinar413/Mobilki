package com.dinar.myproject.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private val KEY_USER_ID = longPreferencesKey("user_id")
    private val KEY_CURRENCY = stringPreferencesKey("currency")

    private val KEY_RATES_JSON = stringPreferencesKey("rates_json")
    private val KEY_RATES_TIME = longPreferencesKey("rates_time")

    private val KEY_LANG = stringPreferencesKey("lang")
    private val KEY_THEME = stringPreferencesKey("theme")


    val userIdFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: 0L
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY] ?: "RUB"
    }

    val ratesJsonFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_RATES_JSON] ?: ""
    }

    val ratesTimeFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_RATES_TIME] ?: 0L
    }

    val langFlow: Flow<String> = context.dataStore.data.map {
        it[KEY_LANG] ?: "ru"
    }
    val themeFlow: Flow<String> = context.dataStore.data.map {
        it[KEY_THEME] ?: "dark"
    }

    suspend fun setLang(code: String) {
        context.dataStore.edit { it[KEY_LANG] = code }
    }
    suspend fun setTheme(code: String) {
        context.dataStore.edit { it[KEY_THEME] = code }
    }

    suspend fun setUserId(id: Long) {
        context.dataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun logout() {
        context.dataStore.edit { it[KEY_USER_ID] = 0L }
    }

    suspend fun setCurrency(code: String) {
        context.dataStore.edit { it[KEY_CURRENCY] = code }
    }

    suspend fun saveRates(json: String, timeMillis: Long) {
        context.dataStore.edit {
            it[KEY_RATES_JSON] = json
            it[KEY_RATES_TIME] = timeMillis
        }
    }

}
