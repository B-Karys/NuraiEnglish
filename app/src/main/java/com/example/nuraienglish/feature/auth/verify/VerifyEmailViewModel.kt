package com.example.nuraienglish.feature.auth.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyEmailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendSuccess: Boolean = false,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyEmailUiState())
    val state = _state.asStateFlow()

    val email: String get() = authRepository.currentEmail

    /** Reload Firebase user and check isEmailVerified. */
    fun checkVerified(onVerified: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resendSuccess = false)
            authRepository.reloadCurrentUser()
            if (authRepository.isEmailVerified) {
                onVerified()
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Email not verified yet. Please click the link in your inbox."
                )
            }
        }
    }

    /** Send another verification email. */
    fun resendEmail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resendSuccess = false)
            authRepository.resendVerificationEmail()
            _state.value = _state.value.copy(isLoading = false, resendSuccess = true)
        }
    }

    /** Sign out and go back to login. */
    fun cancel(onCancel: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onCancel()
        }
    }
}
