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
    LaunchedEffect(authState) {
        if (authState?.isSuccess == true) {
            Toast.makeText(context, "Login successfully", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        } else if (authState?.isFailure == true) {
            Toast.makeText(context, authState?.exceptionOrNull()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
        }
    }
    
    Column(
        Modifier.fillMaxSize().background(BackgroundWhite).padding(24.dp),
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
            Text("Don't have an account? ", color = TextLight)
            Text("Register now", color = PrimaryPink, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRegisterClick() })
        }
    }
}
