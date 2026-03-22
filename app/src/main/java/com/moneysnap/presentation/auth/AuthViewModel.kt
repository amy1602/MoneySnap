package com.moneysnap.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneysnap.domain.repository.AuthRepository
import com.moneysnap.data.repository.AuthRepositoryImpl
import com.moneysnap.domain.usecase.CheckUserLoggedInUseCase
import com.moneysnap.domain.usecase.GoogleLoginUseCase
import com.moneysnap.domain.usecase.LoginUseCase
import com.moneysnap.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(AuthRepositoryImpl()),
    private val registerUseCase: RegisterUseCase = RegisterUseCase(AuthRepositoryImpl()),
    private val googleLoginUseCase: GoogleLoginUseCase = GoogleLoginUseCase(AuthRepositoryImpl()),
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase = CheckUserLoggedInUseCase(AuthRepositoryImpl())
) : ViewModel() {

    val isLoggedIn: Boolean
        get() = checkUserLoggedInUseCase()

    private val _authState = MutableStateFlow<Result<String>?>(null)
    val authState = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) return
        viewModelScope.launch {
            val result = loginUseCase(email, pass)
            _authState.value = result
        }
    }

    fun validateRegistration(name: String, email: String, pass: String, confirmPass: String, agreed: Boolean): String? {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) return "Please fill all fields"
        if (name.length > 50) return "Name cannot be more than 50 characters"
        if (!agreed) return "Please agree to the Terms of Service"
        if (pass.length < 8) return "Password must be at least 8 characters"
        if (pass != confirmPass) return "Passwords do not match"
        return null
    }

    fun register(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) return
        viewModelScope.launch {
            val result = registerUseCase(email, pass)
            _authState.value = result
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) return
        viewModelScope.launch {
            val result = googleLoginUseCase(idToken)
            _authState.value = result
        }
    }
}
