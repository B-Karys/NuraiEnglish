package com.example.nuraienglish.feature.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Lesson

@Composable
fun LessonListScreen(
    courseId: String,
    language: AppLanguage,
    onLessonClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LessonListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.course?.title(language) ?: "Lessons", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (state.lessons.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No lessons available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(state.lessons, key = { _, l -> l.id }) { index, lesson ->
                val completed = state.progress?.completedLessons?.contains(lesson.id) == true
                LessonCard(
                    lesson = lesson,
                    index = index + 1,
                    language = language,
                    isCompleted = completed,
                    onClick = { onLessonClick(lesson.id) }
                )
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, index: Int, language: AppLanguage, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isCompleted) 1f else 0.15f)
            ) {
                Text(
                    text = "$index",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Column(Modifier.weight(1f)) {
                Text(lesson.title(language), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text("${lesson.taskCount} tasks · +${lesson.pointsReward} pts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isCompleted) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
