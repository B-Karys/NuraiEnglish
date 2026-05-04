package com.example.nuraienglish.feature.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.CourseType
import com.example.nuraienglish.core.data.model.Progress

@Composable
fun CourseListScreen(
    language: AppLanguage,
    onCourseClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CourseListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Courses", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (state.courses.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No courses available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.courses, key = { it.id }) { course ->
                CourseCard(
                    course = course,
                    progress = state.progressMap[course.id],
                    language = language,
                    onClick = { onCourseClick(course.id) }
                )
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, progress: Progress?, language: AppLanguage, onClick: () -> Unit) {
    val typeColor = when (course.type) {
        CourseType.VOCABULARY -> MaterialTheme.colorScheme.primary
        CourseType.GRAMMAR -> MaterialTheme.colorScheme.secondary
        CourseType.LISTENING -> MaterialTheme.colorScheme.tertiary
        CourseType.SPEAKING -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = typeColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        text = course.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = typeColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(course.level, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(course.title(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(course.description(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (progress != null && progress.totalLessons > 0) {
                LinearProgressIndicator(
                    progress = { progress.completionFraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = typeColor
                )
                Text("${progress.completedLessons.size} of ${progress.totalLessons} lessons done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("${course.lessonCount} lessons", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
