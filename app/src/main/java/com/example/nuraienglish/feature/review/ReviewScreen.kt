package com.example.nuraienglish.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.TaskType
import com.example.nuraienglish.core.data.model.nativeLanguage
import com.example.nuraienglish.core.ui.UiStrings
import com.example.nuraienglish.core.ui.rememberSpeakEnglish
import com.example.nuraienglish.core.ui.uiStrings
import com.example.nuraienglish.feature.task.AnswerState
import com.example.nuraienglish.feature.task.components.ListeningTask
import com.example.nuraienglish.feature.task.components.MultipleChoiceTask
import com.example.nuraienglish.feature.task.components.SentenceBuildingTask
import com.example.nuraienglish.feature.task.components.TranslationTask

@Composable
fun ReviewScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val strings = language.uiStrings()
    // nativeLang = the language answers are in (never English, since English is being learned)
    val nativeLang = language.nativeLanguage()
    val speak = rememberSpeakEnglish()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.mistakes.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = { (state.currentIndex + 1f) / state.mistakes.size },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.isFinished -> ReviewDoneScreen(strings = strings, onBack = onBack)
            else -> {
                val mistake = viewModel.currentMistake ?: return@Scaffold

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "${strings.mistakeLabel} ${state.currentIndex + 1} ${strings.lessonsOf} ${state.mistakes.size} · ×${mistake.mistakeCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    when (mistake.task.type) {
                        TaskType.WORD_TRANSLATION, TaskType.SENTENCE_TRANSLATION ->
                            TranslationTask(
                                task = mistake.task,
                                language = nativeLang,
                                strings = strings,
                                answerState = state.answerState,
                                onSubmit = { viewModel.submitAnswer(it, language) },
                                onSpeak = speak
                            )
                        TaskType.MULTIPLE_CHOICE ->
                            MultipleChoiceTask(
                                task = mistake.task,
                                language = nativeLang,
                                strings = strings,
                                answerState = state.answerState,
                                onSubmit = { viewModel.submitAnswer(it, language) },
                                onSpeak = speak
                            )
                        TaskType.SENTENCE_BUILDING ->
                            SentenceBuildingTask(
                                task = mistake.task,
                                language = nativeLang,
                                strings = strings,
                                answerState = state.answerState,
                                onSubmit = { viewModel.submitAnswer(it, language) },
                                onSpeak = speak
                            )
                        TaskType.LISTEN_AND_TRANSLATE, TaskType.LISTEN_AND_WRITE ->
                            ListeningTask(
                                task = mistake.task,
                                language = nativeLang,
                                strings = strings,
                                answerState = state.answerState,
                                onSubmit = { viewModel.submitAnswer(it, language) },
                                onSpeak = speak
                            )
                    }

                    if (state.answerState != AnswerState.IDLE) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.next() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.answerState == AnswerState.CORRECT)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                if (state.answerState == AnswerState.CORRECT) strings.correctContinue
                                else strings.wrongNext,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDoneScreen(strings: UiStrings, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                strings.reviewComplete,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                strings.noMoreMistakes,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(strings.backToHome)
            }
        }
    }
}
