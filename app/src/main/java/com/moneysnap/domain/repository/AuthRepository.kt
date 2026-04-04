package com.moneysnap.domain.repository

/**
 * Interface representing the Domain Layer contract for Authentication.
 * This completely abstracts away the underlying implementation (e.g., Firebase).
 */
interface AuthRepository {
    /**
     * Registers a new user with email and password.
     * @return A Result containing the user ID on success, or an Exception on failure.
     */
    suspend fun register(email: String, pass: String): Result<String>

    /**
     * Logs in an existing user with email and password.
     * @return A Result containing the user ID on success, or an Exception on failure.
     */
    suspend fun login(email: String, pass: String): Result<String>

    /**
     * Logs in a user utilizing a Google ID token.
     * @return A Result containing the user ID on success, or an Exception on failure.
     */
    suspend fun loginWithGoogle(idToken: String): Result<String>

    /**
     * Checks if a user is currently logged in.
     * @return True if a user session exists, false otherwise.
     */
    fun isUserLoggedIn(): Boolean

    /**
     * Logs out the current user.
     */
    fun logout()
}
