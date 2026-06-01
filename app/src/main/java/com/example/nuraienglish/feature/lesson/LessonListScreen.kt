package com.example.nuraienglish.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.ui.UiStrings
import com.example.nuraienglish.core.ui.uiStrings
import com.example.nuraienglish.feature.cards.cardStudyStrings

@Composable
fun LessonListScreen(
    courseId: String,
    language: AppLanguage,
    onStudyClick: (String) -> Unit,
    onLessonClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LessonListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val strings = language.uiStrings()
    val cardStrings = language.cardStudyStrings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.course?.title(language) ?: strings.lessons,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (state.lessons.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.noLessonsAvailable, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(state.lessons, key = { _, lesson -> lesson.id }) { index, lesson ->
                val completed = state.progress?.completedLessons?.contains(lesson.id) == true
                LessonCard(
                    lesson = lesson,
                    index = index + 1,
                    language = language,
                    strings = strings,
                    studyLabel = cardStrings.studyCards,
                    practiceLabel = cardStrings.practiceTasks,
                    isCompleted = completed,
                    onStudyClick = { onStudyClick(lesson.id) },
                    onPracticeClick = { onLessonClick(lesson.id) },
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    index: Int,
    language: AppLanguage,
    strings: UiStrings,
    studyLabel: String,
    practiceLabel: String,
    isCompleted: Boolean,
    onStudyClick: () -> Unit,
    onPracticeClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isCompleted) 1f else 0.15f),
                ) {
                    Text(
                        text = "$index",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        lesson.title(language),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "${lesson.taskCount} ${strings.tasks} · +${lesson.pointsReward} ${strings.pts}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStudyClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(studyLabel)
                }
                OutlinedButton(
                    onClick = onPracticeClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(practiceLabel)
                }
            }
        }
    }
}
