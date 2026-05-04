package com.example.nuraienglish.feature.task.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.feature.task.AnswerState

@Composable
fun SentenceBuildingTask(
    task: Task,
    answerState: AnswerState,
    onSubmit: (String) -> Unit
) {
    var builtSentence by remember(task.id) { mutableStateOf(listOf<String>()) }
    var remainingWords by remember(task.id) { mutableStateOf(task.words.shuffled()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Build the sentence", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(task.questionEn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }

        // Built sentence area
        Card(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            if (builtSentence.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Tap words below to build the sentence", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
            Text("Correct: ${task.correctSentence}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Available words
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
            ) { Text("Check") }
            if (builtSentence.isNotEmpty()) {
                TextButton(onClick = { remainingWords = remainingWords + builtSentence; builtSentence = emptyList() }) {
                    Text("Clear")
                }
            }
        }
    }
}
