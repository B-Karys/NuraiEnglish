package com.example.nuraienglish.feature.auth.login

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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, error = null) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, error = null) }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { appPreferences.setLanguage(lang) }
    }

    fun login(
        onSuccess: () -> Unit,
        onNeedsVerification: () -> Unit,
        errorMsg: String = "Please fill in all fields",
        errorMapper: (Throwable) -> String = { it.message ?: "Unknown error" }
    ) {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = errorMsg)
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            authRepository.login(s.email.trim(), s.password)
                .onSuccess {
                    if (authRepository.isEmailVerified) onSuccess()
                    else onNeedsVerification()
                }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = errorMapper(it)) }
        }
    }
}
