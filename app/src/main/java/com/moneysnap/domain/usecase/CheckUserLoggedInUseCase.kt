package com.moneysnap.domain.usecase

import com.moneysnap.domain.repository.AuthRepository

class CheckUserLoggedInUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Boolean {
        return repository.isUserLoggedIn()
    }
}
