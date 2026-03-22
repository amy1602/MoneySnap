package com.moneysnap.domain.usecase

import com.moneysnap.domain.repository.AuthRepository

class GoogleLoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<String> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("ID token cannot be blank"))
        }
        return repository.loginWithGoogle(idToken)
    }
}
