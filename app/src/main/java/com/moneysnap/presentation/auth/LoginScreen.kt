package com.moneysnap.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.moneysnap.data.local.BiometricPreferences
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.launch
import com.moneysnap.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, color = TextLight, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextLight) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = TextLight
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceGrey,
            unfocusedContainerColor = SurfaceGrey,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LoginScreen(viewModel: AuthViewModel, onRegisterClick: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val biometricPrefs = remember { BiometricPreferences(context) }
    val isBiometricAvailable = remember {
        biometricPrefs.isBiometricEnabled &&
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null &&
        biometricPrefs.biometricUserId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    }

    // Function to trigger biometric prompt
    fun triggerBiometric() {
        val activity = context as? FragmentActivity ?: return
        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(context, "Biometric not available", Toast.LENGTH_SHORT).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onLoginSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(context, errString.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(context, "Fingerprint not recognized", Toast.LENGTH_SHORT).show()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login with Biometric")
            .setSubtitle("Use your fingerprint to sign in")
            .setNegativeButtonText("Use Password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Auto-trigger biometric on launch if enabled
    LaunchedEffect(isBiometricAvailable) {
        if (isBiometricAvailable) {
            triggerBiometric()
        }
    }

    LaunchedEffect(authState) {
        if (authState?.isSuccess == true) {
            onLoginSuccess()
        } else if (authState?.isFailure == true) {
            Toast.makeText(context, authState?.exceptionOrNull()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
        }
    }
    
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(24.dp))
            Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Please enter your details to sign in", fontSize = 14.sp, color = TextLight)
            Spacer(Modifier.height(32.dp))
            
            CustomTextField(email, { email = it }, "name@example.com", Icons.Filled.Email)
            Spacer(Modifier.height(16.dp))
            CustomTextField(password, { password = it }, "Enter your password", Icons.Filled.Lock, true)
            
            Text(
                "Forgot Password?", color = PrimaryPink, fontSize = 14.sp, 
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp).clickable { }
            )
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.login(email, password) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            
            Spacer(Modifier.height(24.dp))
            Text("Or continue with", color = TextLight, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            
            // Google + Biometric row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val rawNonce = UUID.randomUUID().toString()
                                val bytes = rawNonce.toByteArray()
                                val md = MessageDigest.getInstance("SHA-256")
                                val digest = md.digest(bytes)
                                val hashedNonce = digest.joinToString("") { "%02x".format(it) }

                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId("765369903223-sp7qf3e5j256c95bemc6dtojn9c702g9.apps.googleusercontent.com")
                                    .setNonce(hashedNonce)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = context
                                )

                                val credential = result.credential
                                if (credential is androidx.credentials.CustomCredential &&
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    viewModel.loginWithGoogle(idToken)
                                } else {
                                    Toast.makeText(context, "Unexpected credential type", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: GetCredentialException) {
                                Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) { Text("Google", color = TextDark, fontWeight = FontWeight.Bold) }

                // Biometric login button (only show if biometric is enrolled)
                if (isBiometricAvailable) {
                    OutlinedButton(
                        onClick = { triggerBiometric() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Login with fingerprint",
                            tint = PrimaryPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            Row(Modifier.padding(bottom = 16.dp)) {
                Text("Don't have an account? ", color = TextLight)
                Text("Register now", color = PrimaryPink, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRegisterClick() })
            }
        }
    }
}
