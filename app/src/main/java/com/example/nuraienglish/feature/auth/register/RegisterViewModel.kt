package com.example.nuraienglish.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state = _state.asStateFlow()

    fun onDisplayNameChange(v: String) { _state.value = _state.value.copy(displayName = v, error = null) }
    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, error = null) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) { _state.value = _state.value.copy(confirmPassword = v, error = null) }

    fun register(onSuccess: () -> Unit) {
        val s = _state.value
        when {
            s.displayName.isBlank() || s.email.isBlank() || s.password.isBlank() ->
                _state.value = s.copy(error = "Please fill in all fields")
            s.password != s.confirmPassword ->
                _state.value = s.copy(error = "Passwords do not match")
            s.password.length < 6 ->
                _state.value = s.copy(error = "Password must be at least 6 characters")
            else -> viewModelScope.launch {
                _state.value = s.copy(isLoading = true, error = null)
                authRepository.register(s.email.trim(), s.password, s.displayName.trim())
                    .onSuccess { onSuccess() }
                    .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            }
        }
    }
}
