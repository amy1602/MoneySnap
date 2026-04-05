package com.moneysnap.presentation.profile

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.moneysnap.data.local.BiometricPreferences

private val AccentRed = Color(0xFFE55061)
private val BgTop = Color(0xFFF1F5F9)
private val BgBottom = Color(0xFFFFFFFF)

@Composable
fun BiometricAuthScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val biometricPrefs = remember { BiometricPreferences(context) }
    var statusText by remember { mutableStateOf("SCANNING...") }
    var statusColor by remember { mutableStateOf(AccentRed) }

    // Pulsing animation for the scanning indicator
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    // Trigger biometric prompt on launch
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity == null) {
            Toast.makeText(context, "Biometric not supported on this device", Toast.LENGTH_SHORT).show()
            onCancel()
            return@LaunchedEffect
        }

        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Device supports biometric, show the prompt
                val executor = ContextCompat.getMainExecutor(context)
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Save biometric preference
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            biometricPrefs.isBiometricEnabled = true
                            biometricPrefs.biometricUserId = userId
                        }
                        statusText = "VERIFIED ✓"
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        statusText = "CANCELLED"
                        statusColor = Color.Gray
                        Toast.makeText(context, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                        onCancel()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        statusText = "TRY AGAIN"
                        Toast.makeText(context, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                val biometricPrompt = BiometricPrompt(activity, executor, callback)
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Enable Biometric Login")
                    .setSubtitle("Verify your fingerprint to enable biometric login")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(context, "No biometric sensor on this device", Toast.LENGTH_SHORT).show()
                onCancel()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(context, "Biometric sensor is currently unavailable", Toast.LENGTH_SHORT).show()
                onCancel()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(context, "No fingerprint enrolled. Please add one in device settings.", Toast.LENGTH_LONG).show()
                onCancel()
            }

            else -> {
                Toast.makeText(context, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
                onCancel()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgTop, BgBottom),
                    startY = 0f,
                    endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Row: Security Check + MONEY MANAGER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Security",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E),
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "Check",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E),
                            lineHeight = 16.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MONEY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentRed,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "MANAGER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentRed,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Fingerprint Icon in a white card with shadow
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = AccentRed.copy(alpha = 0.1f),
                        spotColor = AccentRed.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint",
                    tint = AccentRed,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "Verify your identity",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "Place your finger on the sensor or use\nFace ID to continue",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scanning indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Cancel Button
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
