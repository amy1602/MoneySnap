package com.moneysnap.presentation.auth

import com.moneysnap.domain.usecase.GoogleLoginUseCase
import com.moneysnap.domain.usecase.LoginUseCase
import com.moneysnap.domain.usecase.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockLoginUseCase: LoginUseCase
    private lateinit var mockRegisterUseCase: RegisterUseCase
    private lateinit var mockGoogleLoginUseCase: GoogleLoginUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockLoginUseCase = mock(LoginUseCase::class.java)
        mockRegisterUseCase = mock(RegisterUseCase::class.java)
        mockGoogleLoginUseCase = mock(GoogleLoginUseCase::class.java)
        viewModel = AuthViewModel(mockLoginUseCase, mockRegisterUseCase, mockGoogleLoginUseCase)
    }

    @Test
    fun testValidRegistrationReturnsNull() {
        val result = viewModel.validateRegistration(
            name = "John Doe",
            email = "john@example.com",
            pass = "password123",
            confirmPass = "password123",
            agreed = true
        )
        assertNull(result)
    }

    @Test
    fun testEmptyFieldNameReturnsError() {
        val result = viewModel.validateRegistration("", "john@example.com", "pass", "pass", true)
        assertEquals("Please fill all fields", result)
    }

    @Test
    fun testEmptyFieldEmailReturnsError() {
        val result = viewModel.validateRegistration("", "john@example.com", "pass", "pass", true)
        assertEquals("Please fill all fields", result)
    }

    @Test
    fun testNameExceeds50CharsReturnsError() {
        val longName = "a".repeat(51)
        val result = viewModel.validateRegistration(longName, "john@example.com", "password123", "password123", true)
        assertEquals("Name cannot be more than 50 characters", result)
    }

    @Test
    fun testNotAgreedReturnsError() {
        val result = viewModel.validateRegistration("John", "john@email.com", "password123", "password123", false)
        assertEquals("Please agree to the Terms of Service", result)
    }

    @Test
    fun testPasswordLessThan8CharsReturnsError() {
        val result = viewModel.validateRegistration("John", "john@email.com", "pass123", "pass123", true)
        assertEquals("Password must be at least 8 characters", result)
    }

    @Test
    fun testPasswordsDoNotMatchReturnsError() {
        val result = viewModel.validateRegistration("John", "john@email.com", "password123", "password321", true)
        assertEquals("Passwords do not match", result)
    }
}
