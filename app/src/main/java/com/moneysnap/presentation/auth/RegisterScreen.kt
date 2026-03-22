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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.launch
import com.moneysnap.presentation.theme.*

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onLoginClick: () -> Unit, onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    LaunchedEffect(authState) {
        if (authState?.isSuccess == true) {
            onRegisterSuccess()
        } else if (authState?.isFailure == true) {
            Toast.makeText(context, authState?.exceptionOrNull()?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        Modifier.fillMaxSize().background(BackgroundWhite).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Start tracking your expenses and save more today", fontSize = 14.sp, color = TextLight)
        Spacer(Modifier.height(32.dp))

        CustomTextField(name, { name = it }, "John Doe", Icons.Filled.Person)
        Spacer(Modifier.height(16.dp))
        CustomTextField(email, { email = it }, "name@example.com", Icons.Filled.Email)
        Spacer(Modifier.height(16.dp))
        CustomTextField(password, { password = it }, "Password", Icons.Filled.Lock, true)
        Spacer(Modifier.height(16.dp))
        CustomTextField(confirmPassword, { confirmPassword = it }, "Confirm Password", Icons.Filled.Lock, true)
        
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = agreed, 
                onCheckedChange = { agreed = it }, 
                colors = CheckboxDefaults.colors(checkedColor = PrimaryPink)
            )
            Text("I agree to the Terms of Service & Privacy Policy", fontSize = 12.sp, color = TextLight)
        }
        
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val errorMessage = viewModel.validateRegistration(name, email, password, confirmPassword, agreed)
                if (errorMessage != null) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.register(email, password)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        
        Spacer(Modifier.height(24.dp))
        Text("Or register with", color = TextLight, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        
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
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Google", color = TextDark, fontWeight = FontWeight.Bold) }
        
        Spacer(Modifier.weight(1f))
        Row(Modifier.padding(bottom = 16.dp)) {
            Text("Already have an account? ", color = TextLight)
            Text("Login", color = PrimaryPink, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onLoginClick() })
        }
    }
}
