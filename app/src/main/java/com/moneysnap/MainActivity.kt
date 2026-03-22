package com.moneysnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.moneysnap.presentation.navigation.AppNavigation
import com.moneysnap.presentation.theme.MoneySnapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneySnapTheme {
                AppNavigation()
            }
        }
    }
}
