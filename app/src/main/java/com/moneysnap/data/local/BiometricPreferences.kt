package com.moneysnap.data.local

import android.content.Context
import android.content.SharedPreferences

class BiometricPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var biometricUserId: String?
        get() = prefs.getString(KEY_BIOMETRIC_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_BIOMETRIC_USER_ID, value).apply()

    fun clearBiometric() {
        prefs.edit()
            .remove(KEY_BIOMETRIC_ENABLED)
            .remove(KEY_BIOMETRIC_USER_ID)
            .apply()
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_USER_ID = "biometric_user_id"
    }
}
