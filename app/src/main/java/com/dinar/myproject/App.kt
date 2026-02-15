package com.dinar.myproject

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.dinar.myproject.data.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)

        CoroutineScope(Dispatchers.Main).launch {
            val lang = ServiceLocator.session.langFlow.first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))

            val theme = ServiceLocator.session.themeFlow.first()
            val mode = if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
