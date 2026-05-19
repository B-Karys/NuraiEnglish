package com.example.nuraienglish.feature.task.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType
import com.example.nuraienglish.core.ui.UiStrings
import com.example.nuraienglish.feature.task.AnswerState

@Composable
fun TranslationTask(
    task: Task,
    language: AppLanguage,
    strings: UiStrings,
    answerState: AnswerState,
    onSubmit: (String) -> Unit,
    onSpeak: ((String) -> Unit)? = null
) {
    var answer by remember(task.id) { mutableStateOf("") }
    val label = if (task.type == TaskType.WORD_TRANSLATION) strings.translateWord
                else strings.translateSentence

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = task.questionEn,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(end = if (onSpeak != null) 32.dp else 0.dp)
                )
                if (onSpeak != null) {
                    IconButton(
                        onClick = { onSpeak(task.questionEn) },
                        modifier = Modifier.align(Alignment.TopEnd).size(36.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        OutlinedTextField(
            value = answer,
            onValueChange = { if (answerState == AnswerState.IDLE) answer = it },
            label = { Text(strings.yourAnswer) },
            modifier = Modifier.fillMaxWidth(),
            enabled = answerState == AnswerState.IDLE,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (answer.isNotBlank()) onSubmit(answer) }),
            supportingText = if (answerState == AnswerState.WRONG) ({
                Text(
                    "${strings.correctPrefix}${task.answer(language)}",
                    color = MaterialTheme.colorScheme.error
                )
            }) else null,
            isError = answerState == AnswerState.WRONG
        )

        if (answerState == AnswerState.IDLE) {
            Button(
                onClick = { if (answer.isNotBlank()) onSubmit(answer) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = answer.isNotBlank()
            ) {
                Text(strings.check)
            }
        }
    }
}
