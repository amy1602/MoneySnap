package com.moneysnap

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.moneysnap.presentation.navigation.AppNavigation
import com.moneysnap.presentation.theme.MoneySnapTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneySnapTheme {
                AppNavigation()
            }
        }
    }
}
