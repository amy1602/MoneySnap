package com.moneysnap.domain.usecase

import com.moneysnap.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, pass: String): Result<String> {
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be blank"))
        }
        return repository.login(email, pass)
    }
}
