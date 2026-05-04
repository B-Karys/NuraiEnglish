package com.example.nuraienglish.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, error = null) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, error = null) }

    fun login(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            authRepository.login(s.email.trim(), s.password)
                .onSuccess { onSuccess() }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }
}
