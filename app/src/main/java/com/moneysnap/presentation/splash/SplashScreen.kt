package com.moneysnap.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneysnap.presentation.theme.BackgroundWhite
import com.moneysnap.presentation.theme.PrimaryPink
import com.moneysnap.presentation.theme.TextDark
import com.moneysnap.presentation.theme.TextLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToNext: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1000)
        onNavigateToNext()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundWhite).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Logo", tint = PrimaryPink, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Money Manager", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Smart tracking for your future", fontSize = 14.sp, color = TextLight)
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(Icons.Filled.Pets, contentDescription = "Vault Icon", tint = PrimaryPink, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("INITIALISING SECURE VAULT", fontSize = 10.sp, color = TextLight, letterSpacing = 1.sp)
        Text("Version 2.4.0", fontSize = 10.sp, color = TextLight)
    }
}
