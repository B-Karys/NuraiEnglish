package com.example.nuraienglish.feature.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            if (state.step != RegisterStep.EMAIL) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.systemBars)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.step) {
                RegisterStep.EMAIL -> EmailStep(
                    state = state,
                    onEmailChange = viewModel::onEmailChange,
                    onSendCode = viewModel::sendCode,
                    onNavigateToLogin = onNavigateToLogin
                )
                RegisterStep.VERIFY_CODE -> VerifyCodeStep(
                    state = state,
                    onCodeChange = viewModel::onCodeChange,
                    onVerify = viewModel::verifyCode,
                    onResend = viewModel::sendCode
                )
                RegisterStep.ACCOUNT_DETAILS -> AccountDetailsStep(
                    state = state,
                    onDisplayNameChange = viewModel::onDisplayNameChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onRegister = { viewModel.register(onRegisterSuccess) }
                )
            }
        }
    }
}

@Composable
private fun EmailStep(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Text("Create Account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(
        "We'll send a verification code to your email",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(32.dp))

    OutlinedTextField(
        value = state.email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        isError = state.error != null
    )

    ErrorText(state.error)

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onSendCode,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading
    ) {
        if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        else Text("Send verification code", style = MaterialTheme.typography.labelLarge)
    }

    Spacer(Modifier.height(16.dp))
    TextButton(onClick = onNavigateToLogin) { Text("Already have an account? Sign in") }
}

@Composable
private fun VerifyCodeStep(
    state: RegisterUiState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    Text("Check your email", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(
        "Enter the 6-digit code sent to ${state.email}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(32.dp))

    OutlinedTextField(
        value = state.code,
        onValueChange = { if (it.length <= 6) onCodeChange(it) },
        label = { Text("Verification code") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = state.error != null
    )

    ErrorText(state.error)

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onVerify,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading
    ) {
        if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        else Text("Verify code", style = MaterialTheme.typography.labelLarge)
    }

    Spacer(Modifier.height(12.dp))
    TextButton(onClick = onResend, enabled = !state.isLoading) { Text("Resend code") }
}

@Composable
private fun AccountDetailsStep(
    state: RegisterUiState,
    onDisplayNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: () -> Unit
) {
    Text("Almost done!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text("Set your name and password", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(Modifier.height(32.dp))

    OutlinedTextField(
        value = state.displayName,
        onValueChange = onDisplayNameChange,
        label = { Text("Your name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = state.error != null
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = state.error != null
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = state.confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirm password") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = state.error != null
    )

    ErrorText(state.error)

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onRegister,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading
    ) {
        if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        else Text("Create Account", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ErrorText(error: String?) {
    if (error != null) {
        Spacer(Modifier.height(8.dp))
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}
