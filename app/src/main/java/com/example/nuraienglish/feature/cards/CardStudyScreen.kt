package com.example.nuraienglish.feature.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.LearningCard
import com.example.nuraienglish.core.data.model.LearningCardType
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType
import com.example.nuraienglish.core.data.model.nativeLanguage
import com.example.nuraienglish.core.ui.rememberSpeakEnglish

data class StudyCard(
    val id: String,
    val category: String,
    val frontTitle: String,
    val frontText: String,
    val backTitle: String,
    val backText: String,
    val note: String,
    val speakText: String,
)

@Composable
fun CardStudyScreen(
    courseId: String,
    lessonId: String,
    language: AppLanguage,
    onStartPractice: () -> Unit,
    onBack: () -> Unit,
    viewModel: CardStudyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val strings = language.cardStudyStrings()
    val speak = rememberSpeakEnglish()
    val cards = state.learningCards.map { it.toStudyCard(language, strings) } +
        state.tasks.map { it.toStudyCard(language, strings) }
    val currentCard = cards.getOrNull(state.currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (cards.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = { (state.currentIndex + 1f) / cards.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Text(strings.cardsTitle, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            cards.isEmpty() -> EmptyCardsState(
                strings = strings,
                padding = padding,
                onStartPractice = onStartPractice,
            )
            state.isFinished -> CardsCompleteState(
                strings = strings,
                padding = padding,
                onReviewAgain = viewModel::reviewAgain,
                onStartPractice = onStartPractice,
            )
            currentCard != null -> CardStudyContent(
                card = currentCard,
                currentIndex = state.currentIndex,
                cardCount = cards.size,
                isRevealed = state.isRevealed,
                strings = strings,
                padding = padding,
                onSpeak = speak,
                onReveal = viewModel::reveal,
                onToggleReveal = viewModel::toggleReveal,
                onPrevious = viewModel::previous,
                onNext = { viewModel.next(cards.size) },
            )
        }
    }
}

@Composable
private fun CardStudyContent(
    card: StudyCard,
    currentIndex: Int,
    cardCount: Int,
    isRevealed: Boolean,
    strings: CardStudyStrings,
    padding: PaddingValues,
    onSpeak: (String) -> Unit,
    onReveal: () -> Unit,
    onToggleReveal: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${currentIndex + 1} ${strings.cardOf} $cardCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = card.category,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        LearningCard(
            card = card,
            isRevealed = isRevealed,
            strings = strings,
            onSpeak = onSpeak,
            onClick = onToggleReveal,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
            ) {
                Text(strings.previous)
            }
            Button(
                onClick = if (isRevealed) onNext else onReveal,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
            ) {
                Text(if (isRevealed) strings.next else strings.reveal)
            }
        }
    }
}

@Composable
private fun LearningCard(
    card: StudyCard,
    isRevealed: Boolean,
    strings: CardStudyStrings,
    onSpeak: (String) -> Unit,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isRevealed) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Box(Modifier.fillMaxSize().padding(22.dp)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (isRevealed) card.backTitle else card.frontTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (isRevealed) card.backText else card.frontText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                if (isRevealed && card.note.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(strings.note, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(card.note, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            IconButton(
                onClick = { onSpeak(card.speakText) },
                modifier = Modifier.align(Alignment.TopEnd),
                enabled = card.speakText.isNotBlank(),
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = if (isRevealed) strings.tapToHide else strings.tapToReveal,
                modifier = Modifier.align(Alignment.BottomCenter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyCardsState(
    strings: CardStudyStrings,
    padding: PaddingValues,
    onStartPractice: () -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(strings.noCards, textAlign = TextAlign.Center)
            Button(onClick = onStartPractice, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(strings.startPractice)
            }
        }
    }
}

@Composable
private fun CardsCompleteState(
    strings: CardStudyStrings,
    padding: PaddingValues,
    onReviewAgain: () -> Unit,
    onStartPractice: () -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                strings.cardsComplete,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onStartPractice, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(strings.startPractice)
            }
            TextButton(onClick = onReviewAgain, modifier = Modifier.fillMaxWidth()) {
                Text(strings.reviewAgain)
            }
        }
    }
}

private fun Task.toStudyCard(language: AppLanguage, strings: CardStudyStrings): StudyCard {
    val nativeLanguage = language.nativeLanguage()
    val nativeQuestion = question(nativeLanguage).ifBlank { questionEn }
    val nativeAnswer = answer(nativeLanguage).ifBlank { answerEn }
    val englishAnswer = answerEn.ifBlank { correctSentence.ifBlank { questionEn } }

    return when (type) {
        TaskType.WORD_TRANSLATION -> StudyCard(
            id = id,
            category = strings.wordCard,
            frontTitle = strings.wordCard,
            frontText = questionEn,
            backTitle = strings.translation,
            backText = nativeAnswer,
            note = nativeQuestion.takeIf { it != questionEn }.orEmpty().ifBlank { strings.wordNote },
            speakText = questionEn,
        )
        TaskType.SENTENCE_TRANSLATION -> StudyCard(
            id = id,
            category = strings.sentenceCard,
            frontTitle = strings.phraseCard,
            frontText = questionEn,
            backTitle = strings.translation,
            backText = nativeAnswer,
            note = strings.sentenceNote,
            speakText = questionEn,
        )
        TaskType.MULTIPLE_CHOICE -> StudyCard(
            id = id,
            category = strings.grammarCard,
            frontTitle = strings.grammarCard,
            frontText = nativeQuestion,
            backTitle = strings.correctForm,
            backText = englishAnswer,
            note = strings.grammarNote,
            speakText = englishAnswer,
        )
        TaskType.SENTENCE_BUILDING -> StudyCard(
            id = id,
            category = strings.sentenceCard,
            frontTitle = strings.sentenceCard,
            frontText = nativeQuestion,
            backTitle = strings.englishSentence,
            backText = correctSentence.ifBlank { englishAnswer },
            note = if (words.isNotEmpty()) {
                "${strings.wordsToUse}: ${words.joinToString(", ")}"
            } else {
                strings.sentenceNote
            },
            speakText = correctSentence.ifBlank { englishAnswer },
        )
        TaskType.LISTEN_AND_TRANSLATE,
        TaskType.LISTEN_AND_WRITE -> StudyCard(
            id = id,
            category = strings.listeningCard,
            frontTitle = strings.listeningCard,
            frontText = strings.listenFirst,
            backTitle = strings.translation,
            backText = listOf(questionEn, nativeAnswer)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString("\n"),
            note = strings.listeningNote,
            speakText = questionEn,
        )
    }
}

private fun LearningCard.toStudyCard(language: AppLanguage, strings: CardStudyStrings): StudyCard {
    val front = front(language)
    val back = back(language)
    val spoken = speakText.ifBlank { frontEn.ifBlank { front } }

    return StudyCard(
        id = id,
        category = type.label(strings),
        frontTitle = type.label(strings),
        frontText = front,
        backTitle = strings.translation,
        backText = back,
        note = note(language),
        speakText = spoken,
    )
}

private fun LearningCardType.label(strings: CardStudyStrings) = when (this) {
    LearningCardType.VOCABULARY -> strings.wordCard
    LearningCardType.GRAMMAR -> strings.grammarCard
    LearningCardType.PHRASE -> strings.phraseCard
    LearningCardType.LISTENING -> strings.listeningCard
    LearningCardType.CUSTOM -> strings.studyCards
}
