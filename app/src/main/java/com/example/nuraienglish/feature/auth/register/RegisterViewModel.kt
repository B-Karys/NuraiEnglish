package com.example.nuraienglish.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.preferences.AppPreferences
import com.example.nuraienglish.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val displayName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(v: String)           { _state.value = _state.value.copy(email = v, error = null) }
    fun onDisplayNameChange(v: String)     { _state.value = _state.value.copy(displayName = v, error = null) }
    fun onPasswordChange(v: String)        { _state.value = _state.value.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) { _state.value = _state.value.copy(confirmPassword = v, error = null) }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { appPreferences.setLanguage(lang) }
    }

    fun register(
        onSuccess: () -> Unit,
        errEmail: String = "Enter your email address",
        errName: String = "Enter your name",
        errPasswordShort: String = "Password must be at least 6 characters",
        errPasswordMismatch: String = "Passwords do not match",
        errorMapper: (Throwable) -> String = { it.message ?: "Unknown error" }
    ) {
        val s = _state.value
        when {
            s.email.isBlank()       -> _state.value = s.copy(error = errEmail)
            s.displayName.isBlank() -> _state.value = s.copy(error = errName)
            s.password.length < 6   -> _state.value = s.copy(error = errPasswordShort)
            s.password != s.confirmPassword -> _state.value = s.copy(error = errPasswordMismatch)
            else -> viewModelScope.launch {
                _state.value = s.copy(isLoading = true, error = null)
                authRepository.register(s.email.trim(), s.password, s.displayName.trim())
                    .onSuccess { onSuccess() }
                    .onFailure { _state.value = _state.value.copy(isLoading = false, error = errorMapper(it)) }
            }
        }
    }
}
