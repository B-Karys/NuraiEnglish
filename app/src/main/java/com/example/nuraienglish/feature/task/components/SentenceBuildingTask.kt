package com.example.nuraienglish.feature.task.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun SentenceBuildingTask(
    task: Task,
    language: AppLanguage,
    strings: UiStrings,
    answerState: AnswerState,
    onSubmit: (String) -> Unit,
    onSpeak: ((String) -> Unit)? = null
) {
    var builtSentence by remember(task.id) { mutableStateOf(listOf<String>()) }
    var remainingWords by remember(task.id) { mutableStateOf(task.words.shuffled()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(strings.buildSentence, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    task.question(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(end = if (onSpeak != null) 32.dp else 0.dp)
                )
                // Speaks the English sentence the user needs to build
                if (onSpeak != null) {
                    IconButton(
                        onClick = { onSpeak(task.correctSentence) },
                        modifier = Modifier.align(Alignment.TopEnd).size(36.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        // Built sentence area
        Card(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            if (builtSentence.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(strings.tapWords, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyRow(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(builtSentence) { word ->
                        AssistChip(
                            onClick = {
                                if (answerState == AnswerState.IDLE) {
                                    builtSentence = builtSentence - word
                                    remainingWords = remainingWords + word
                                }
                            },
                            label = { Text(word) }
                        )
                    }
                }
            }
        }

        if (answerState == AnswerState.WRONG) {
            Text(
                "${strings.correctPrefix}${task.correctSentence}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Available words
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(remainingWords) { word ->
                FilterChip(
                    selected = false,
                    onClick = {
                        if (answerState == AnswerState.IDLE) {
                            builtSentence = builtSentence + word
                            remainingWords = remainingWords - word
                        }
                    },
                    label = { Text(word) }
                )
            }
        }

        if (answerState == AnswerState.IDLE) {
            Button(
                onClick = { onSubmit(builtSentence.joinToString(" ")) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = builtSentence.isNotEmpty()
            ) {
                Text(strings.check)
            }
            if (builtSentence.isNotEmpty()) {
                TextButton(onClick = {
                    remainingWords = remainingWords + builtSentence
                    builtSentence = emptyList()
                }) {
                    Text(strings.clear)
                }
            }
        }
    }
}
