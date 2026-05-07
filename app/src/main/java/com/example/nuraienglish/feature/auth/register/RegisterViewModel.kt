package com.example.nuraienglish.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.remote.VerificationRepository
import com.example.nuraienglish.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RegisterStep { EMAIL, VERIFY_CODE, ACCOUNT_DETAILS }

data class RegisterUiState(
    val step: RegisterStep = RegisterStep.EMAIL,
    val email: String = "",
    val code: String = "",
    val displayName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val verificationRepository: VerificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, error = null) }
    fun onCodeChange(v: String) { _state.value = _state.value.copy(code = v, error = null) }
    fun onDisplayNameChange(v: String) { _state.value = _state.value.copy(displayName = v, error = null) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) { _state.value = _state.value.copy(confirmPassword = v, error = null) }

    fun sendCode() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.value = _state.value.copy(error = "Enter your email address")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            verificationRepository.sendCode(email)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, step = RegisterStep.VERIFY_CODE)
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false, error = it.message ?: "Failed to send code")
                }
        }
    }

    fun verifyCode() {
        val s = _state.value
        if (s.code.length != 6) {
            _state.value = s.copy(error = "Enter the 6-digit code from your email")
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            verificationRepository.verifyCode(s.email.trim(), s.code.trim())
                .onSuccess { valid ->
                    if (valid) {
                        _state.value = _state.value.copy(isLoading = false, step = RegisterStep.ACCOUNT_DETAILS)
                    } else {
                        _state.value = _state.value.copy(isLoading = false, error = "Incorrect or expired code")
                    }
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false, error = it.message ?: "Verification failed")
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val s = _state.value
        when {
            s.displayName.isBlank() -> _state.value = s.copy(error = "Enter your name")
            s.password.length < 6 -> _state.value = s.copy(error = "Password must be at least 6 characters")
            s.password != s.confirmPassword -> _state.value = s.copy(error = "Passwords do not match")
            else -> viewModelScope.launch {
                _state.value = s.copy(isLoading = true, error = null)
                authRepository.register(s.email.trim(), s.password, s.displayName.trim())
                    .onSuccess { onSuccess() }
                    .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            }
        }
    }

    fun goBack() {
        val prev = when (_state.value.step) {
            RegisterStep.VERIFY_CODE -> RegisterStep.EMAIL
            RegisterStep.ACCOUNT_DETAILS -> RegisterStep.VERIFY_CODE
            RegisterStep.EMAIL -> RegisterStep.EMAIL
        }
        _state.value = _state.value.copy(step = prev, error = null)
    }
}
