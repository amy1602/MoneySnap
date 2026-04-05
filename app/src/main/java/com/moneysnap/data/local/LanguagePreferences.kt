package com.moneysnap.data.local

import android.content.Context
import android.content.SharedPreferences

class LanguagePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    var selectedLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    companion object {
        private const val KEY_LANGUAGE = "selected_language"
    }
}
