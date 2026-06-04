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
import com.example.nuraienglish.core.data.model.canBeOpenedBy
import com.example.nuraienglish.core.data.model.pointsNeeded
import com.example.nuraienglish.core.data.model.requiredPointsToOpen
import com.example.nuraienglish.core.ui.uiStrings

@Composable
fun CourseListScreen(
    language: AppLanguage,
    onCourseClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CourseListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val strings = language.uiStrings()
    var lockedCourseTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.allCourses, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (state.courses.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.noCoursesAvailable, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            lockedCourseTitle?.let { title ->
                item {
                    LockedCourseNotice(
                        title = title,
                        points = state.totalPoints,
                        strings = strings
                    )
                }
            }
            items(state.courses, key = { it.id }) { course ->
                val isUnlocked = course.canBeOpenedBy(state.totalPoints, state.isAdmin)
                CourseCard(
                    course = course,
                    progress = state.progressMap[course.id],
                    language = language,
                    totalPoints = state.totalPoints,
                    isAdmin = state.isAdmin,
                    onClick = {
                        if (isUnlocked) {
                            lockedCourseTitle = null
                            onCourseClick(course.id)
                        } else {
                            lockedCourseTitle = course.title(language)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LockedCourseNotice(
    title: String,
    points: Int,
    strings: com.example.nuraienglish.core.ui.UiStrings
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            "$title is locked. You have $points ${strings.pts}.",
            modifier = Modifier.padding(14.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CourseCard(
    course: Course,
    progress: Progress?,
    language: AppLanguage,
    totalPoints: Int,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    val strings = language.uiStrings()
    val isUnlocked = course.canBeOpenedBy(totalPoints, isAdmin)
    val typeColor = when (course.type) {
        CourseType.VOCABULARY -> MaterialTheme.colorScheme.primary
        CourseType.GRAMMAR    -> MaterialTheme.colorScheme.secondary
        CourseType.LISTENING  -> MaterialTheme.colorScheme.tertiary
    }
    val accentColor = if (isUnlocked) typeColor else MaterialTheme.colorScheme.onSurfaceVariant
    val typeLabel = when (course.type) {
        CourseType.VOCABULARY -> strings.typeVocabulary
        CourseType.GRAMMAR    -> strings.typeGrammar
        CourseType.LISTENING  -> strings.typeListening
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            }
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = accentColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = accentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    if (isUnlocked) course.level else "Locked",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                course.title(language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                course.description(language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (progress != null && progress.totalLessons > 0) {
                LinearProgressIndicator(
                    progress = { progress.completionFraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = accentColor
                )
                Text(
                    if (isUnlocked) {
                        "${progress.completedLessons.size} ${strings.lessonsOf} ${progress.totalLessons} ${strings.lessonsDone}"
                    } else {
                        courseAccessLabel(course, totalPoints, isAdmin, strings)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    courseAccessLabel(course, totalPoints, isAdmin, strings),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun courseAccessLabel(
    course: Course,
    totalPoints: Int,
    isAdmin: Boolean,
    strings: com.example.nuraienglish.core.ui.UiStrings
): String {
    val requiredPoints = course.requiredPointsToOpen()
    return when {
        isAdmin && requiredPoints > 0 -> "${course.lessonCount} ${strings.lessons} - admin access"
        requiredPoints == 0 -> "${course.lessonCount} ${strings.lessons}"
        course.canBeOpenedBy(totalPoints, isAdmin) -> "${course.lessonCount} ${strings.lessons} - $requiredPoints ${strings.pts}"
        else -> "${course.lessonCount} ${strings.lessons} - needs ${course.pointsNeeded(totalPoints)} ${strings.pts}"
    }
}
