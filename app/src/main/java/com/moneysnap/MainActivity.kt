package com.moneysnap

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.FragmentActivity
import com.moneysnap.data.local.LanguagePreferences
import com.moneysnap.presentation.navigation.AppNavigation
import com.moneysnap.presentation.theme.MoneySnapTheme
import java.util.Locale

val LocaleChangeCallback = staticCompositionLocalOf<() -> Unit> { {} }
val LocalImportFilePicker = staticCompositionLocalOf<(onResult: (Uri?) -> Unit) -> Unit> { {} }

class MainActivity : FragmentActivity() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private var filePickerCallback: ((Uri?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register file picker at Activity level to avoid FragmentActivity requestCode limit
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            filePickerCallback?.invoke(uri)
            filePickerCallback = null
        }

        setContent {
            CompositionLocalProvider(
                LocaleChangeCallback provides { recreate() },
                LocalImportFilePicker provides { callback ->
                    filePickerCallback = callback
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    filePickerLauncher.launch(intent)
                }
            ) {
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
