package com.dinar.myproject.ui.main

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val currencyCodes = listOf("RUB", "TRY", "EUR", "USD")
    private val currencyTitles = listOf("Рубли (₽)", "Турецкие лиры (₺)", "Евро (€)", "Доллар ($)")

    private val langCodes = listOf("ru", "en", "tr")
    private val langTitles = listOf("Русский", "English", "Türkçe")

    private val themeCodes = listOf("dark", "light")
    private val themeTitles = listOf("Тёмная", "Светлая")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvRole = view.findViewById<TextView>(R.id.tvSettingsRole)

        val spCurrency = view.findViewById<Spinner>(R.id.spSettingsCurrency)
        val spLang = view.findViewById<Spinner>(R.id.spSettingsLang)
        val spTheme = view.findViewById<Spinner>(R.id.spSettingsTheme)

        val btnLogout = view.findViewById<Button>(R.id.btnSettingsLogout)

        spCurrency.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencyTitles)
        spLang.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, langTitles)
        spTheme.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themeTitles)

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = ServiceLocator.session.userIdFlow.first()
            val role = withContext(Dispatchers.IO) { ServiceLocator.financeRepo.getUserRole(userId) }
            tvRole.text = getString(R.string.settings_role, role)

            val cur = ServiceLocator.session.currencyFlow.first()
            spCurrency.setSelection(currencyCodes.indexOf(cur).coerceAtLeast(0))

            val lang = ServiceLocator.session.langFlow.first()
            spLang.setSelection(langCodes.indexOf(lang).coerceAtLeast(0))

            val theme = ServiceLocator.session.themeFlow.first()
            spTheme.setSelection(themeCodes.indexOf(theme).coerceAtLeast(0))
        }

        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    ServiceLocator.session.setCurrency(currencyCodes[pos])
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                val code = langCodes[pos]
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    ServiceLocator.session.setLang(code)
                }
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                val code = themeCodes[pos]
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    ServiceLocator.session.setTheme(code)
                }
                val mode = if (code == "dark") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(mode)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                ServiceLocator.session.logout()
                withContext(Dispatchers.Main) {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
    }
}
