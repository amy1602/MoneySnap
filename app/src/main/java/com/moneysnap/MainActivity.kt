package com.moneysnap

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.FragmentActivity
import com.moneysnap.data.local.LanguagePreferences
import com.moneysnap.presentation.navigation.AppNavigation
import com.moneysnap.presentation.theme.MoneySnapTheme
import java.util.Locale

val LocaleChangeCallback = staticCompositionLocalOf<() -> Unit> { {} }

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocaleChangeCallback provides { recreate() }) {
                MoneySnapTheme {
                    AppNavigation()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val languagePrefs = LanguagePreferences(newBase)
        val locale = Locale(languagePrefs.selectedLanguage)
        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
