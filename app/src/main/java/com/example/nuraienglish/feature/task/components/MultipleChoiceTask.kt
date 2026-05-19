package com.example.nuraienglish.feature.task.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.ui.UiStrings
import com.example.nuraienglish.feature.task.AnswerState

@Composable
fun MultipleChoiceTask(
    task: Task,
    language: AppLanguage,
    strings: UiStrings,
    answerState: AnswerState,
    onSubmit: (String) -> Unit,
    onSpeak: ((String) -> Unit)? = null
) {
    var selected by remember(task.id) { mutableStateOf<String?>(null) }
    val correctAnswer = task.answer(language)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(strings.chooseCorrect, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    task.questionEn,
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

        task.options(language).forEach { option ->
            val isSelected = selected == option
            val isCorrect = answerState != AnswerState.IDLE && option == correctAnswer
            val isWrong = answerState == AnswerState.WRONG && isSelected

            val containerColor = when {
                isCorrect -> MaterialTheme.colorScheme.primaryContainer
                isWrong   -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else       -> MaterialTheme.colorScheme.surfaceContainer
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                onClick = { if (answerState == AnswerState.IDLE) { selected = option; onSubmit(option) } }
            ) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
