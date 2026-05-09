package com.example.nuraienglish.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
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
import com.example.nuraienglish.core.ui.uiStrings

@Composable
fun HomeScreen(
    language: AppLanguage,
    onNavigateToCourses: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val strings = language.uiStrings()
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            current = language,
            strings = strings,
            onSelect = { viewModel.setLanguage(it.code) },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WordLy", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = strings.interfaceLanguage)
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
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

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Points banner ─────────────────────────────────────────────
            item {
                PointsBanner(
                    name = state.user?.displayName ?: "",
                    points = state.totalPoints,
                    level = state.user?.currentLevel ?: "A1",
                    strings = strings,
                )
            }

            // ── Quick actions ─────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = strings.myProgress,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProgress
                    )
                    QuickActionCard(
                        title = strings.review,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReview
                    )
                }
            }

            // ── Courses header ────────────────────────────────────────────
            if (state.courses.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.courses,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToCourses) {
                            Text(strings.seeAll)
                        }
                    }
                }

                // ── Course cards — vertical list ──────────────────────────
                items(state.courses.take(6), key = { it.id }) { course ->
                    HomeCourseCard(
                        course = course,
                        progress = state.progressMap[course.id],
                        language = language,
                        strings = strings,
                        onClick = onNavigateToCourses
                    )
                }
            } else {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Box(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                strings.noCoursesYet,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Admin Panel button ────────────────────────────────────────
            if (state.isAdmin) {
                item {
                    Button(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.adminPanel)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PointsBanner(
    name: String,
    points: Int,
    level: String,
    strings: com.example.nuraienglish.core.ui.UiStrings,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    level,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column {
                if (name.isNotBlank()) {
                    Text(
                        "${strings.hello}, $name!",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    "$points ${strings.pointsEarned}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            Modifier.padding(16.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun HomeCourseCard(
    course: Course,
    progress: Progress?,
    language: AppLanguage,
    strings: com.example.nuraienglish.core.ui.UiStrings,
    onClick: () -> Unit,
) {
    val typeColor = when (course.type) {
        CourseType.VOCABULARY -> MaterialTheme.colorScheme.primary
        CourseType.GRAMMAR    -> MaterialTheme.colorScheme.secondary
        CourseType.LISTENING  -> MaterialTheme.colorScheme.tertiary
        CourseType.SPEAKING   -> MaterialTheme.colorScheme.error
    }
    val typeLabel = when (course.type) {
        CourseType.VOCABULARY -> strings.typeVocabulary
        CourseType.GRAMMAR    -> strings.typeGrammar
        CourseType.LISTENING  -> strings.typeListening
        CourseType.SPEAKING   -> strings.typeSpeaking
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = typeColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = typeColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    course.level,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                course.title(language),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                course.description(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (progress != null && progress.totalLessons > 0) {
                LinearProgressIndicator(
                    progress = { progress.completionFraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = typeColor
                )
                Text(
                    "${progress.completedLessons.size} ${strings.lessonsOf} ${progress.totalLessons} ${strings.lessonsDone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "${course.lessonCount} ${strings.lessons}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    current: AppLanguage,
    strings: com.example.nuraienglish.core.ui.UiStrings,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.interfaceLanguage) },
        text = {
            Column {
                AppLanguage.entries.forEach { lang ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = lang == current,
                            onClick = { onSelect(lang); onDismiss() }
                        )
                        Text(lang.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}
