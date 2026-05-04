package com.example.nuraienglish.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage

@Composable
fun ReviewScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Mistakes", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.isFinished -> ReviewDoneScreen(onBack = onBack)
            else -> {
                val mistake = viewModel.currentMistake ?: return@Scaffold
                var answer by remember(mistake.taskId) { mutableStateOf("") }

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Mistake ${state.currentIndex + 1} of ${state.mistakes.size} · seen ${mistake.mistakeCount}×",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                mistake.task.questionEn,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    OutlinedTextField(
                        value = answer,
                        onValueChange = { if (state.answerState == ReviewAnswerState.IDLE) answer = it },
                        label = { Text("Your answer in ${language.displayName}") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.answerState == ReviewAnswerState.IDLE,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (answer.isNotBlank()) viewModel.submitAnswer(answer, language) }),
                        supportingText = if (state.answerState == ReviewAnswerState.WRONG) ({
                            Text("Correct: ${mistake.task.answer(language)}", color = MaterialTheme.colorScheme.error)
                        }) else null,
                        isError = state.answerState == ReviewAnswerState.WRONG
                    )

                    if (state.answerState == ReviewAnswerState.IDLE) {
                        Button(
                            onClick = { if (answer.isNotBlank()) viewModel.submitAnswer(answer, language) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = answer.isNotBlank()
                        ) { Text("Check") }
                    } else {
                        Button(
                            onClick = { viewModel.next() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.answerState == ReviewAnswerState.CORRECT)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(if (state.answerState == ReviewAnswerState.CORRECT) "Correct! Next" else "Wrong — Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDoneScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("Review Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("No more mistakes to review right now.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("Back to Home")
            }
        }
    }
}
