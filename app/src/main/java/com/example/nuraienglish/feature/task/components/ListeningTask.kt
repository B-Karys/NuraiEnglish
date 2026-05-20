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

/**
 * Composable for LISTEN_AND_TRANSLATE and LISTEN_AND_WRITE task types.
 *
 * - Automatically plays the English audio when the task first appears.
 * - Shows a large tappable audio card to replay at any time.
 * - After the user answers, the English text is revealed inside the card.
 *
 * LISTEN_AND_TRANSLATE: user hears English → types native-language translation.
 * LISTEN_AND_WRITE:     user hears English → types what they heard (dictation).
 */
@Composable
fun ListeningTask(
    task: Task,
    language: AppLanguage,     // already resolved nativeLang — used for LISTEN_AND_TRANSLATE answer
    strings: UiStrings,
    answerState: AnswerState,
    onSubmit: (String) -> Unit,
    onSpeak: ((String) -> Unit)? = null
) {
    val isWriteMode = task.type == TaskType.LISTEN_AND_WRITE
    var answer by remember(task.id) { mutableStateOf("") }

    // Auto-play the audio when this task first appears
    LaunchedEffect(task.id) {
        onSpeak?.invoke(task.questionEn)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isWriteMode) strings.typeWhatYouHear else strings.listenAndTranslate,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tappable audio card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = { onSpeak?.invoke(task.questionEn) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Play audio",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    strings.tapToListenHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Reveal the English text once the user has answered
                if (answerState != AnswerState.IDLE) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        task.questionEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        val correctAnswer = if (isWriteMode) task.answerEn else task.answer(language)

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
                    "${strings.correctPrefix}$correctAnswer",
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
