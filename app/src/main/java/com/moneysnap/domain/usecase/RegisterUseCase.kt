package com.moneysnap.domain.usecase

import com.moneysnap.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, pass: String): Result<String> {
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be blank"))
        }
        if (pass.length < 8) {
            return Result.failure(IllegalArgumentException("Password must be at least 8 characters"))
        }
        return repository.register(email, pass)
    }
}
