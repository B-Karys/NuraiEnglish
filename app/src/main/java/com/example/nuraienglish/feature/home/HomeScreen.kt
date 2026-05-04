package com.example.nuraienglish.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.nuraienglish.core.data.model.Progress

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
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            current = language,
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
                        Icon(Icons.Default.Settings, contentDescription = "Language")
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PointsBanner(
                    name = state.user?.displayName ?: "",
                    points = state.totalPoints,
                    level = state.user?.currentLevel ?: "A1"
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("My Progress", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f), onNavigateToProgress)
                    QuickActionCard("Review", MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f), onNavigateToReview)
                }
            }

            if (state.courses.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Courses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateToCourses) { Text("See all") }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.courses.take(6)) { course ->
                            HomeCourseCard(
                                course = course,
                                progress = state.progressMap[course.id],
                                language = language,
                                onClick = onNavigateToCourses
                            )
                        }
                    }
                }
            } else {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No courses yet. Check back soon!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (state.isAdmin) {
                item {
                    Button(onClick = onNavigateToAdmin, modifier = Modifier.fillMaxWidth()) {
                        Text("Admin Panel")
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsBanner(name: String, points: Int, level: String) {
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
                Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(level, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Column {
                if (name.isNotBlank()) Text("Hello, $name!", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("$points points earned", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun QuickActionCard(title: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = color)) {
        Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun HomeCourseCard(course: Course, progress: Progress?, language: AppLanguage, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(200.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(course.level, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(course.title(language), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(course.description(language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.completionFraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                )
                Text("${progress.completedLessons.size}/${progress.totalLessons} lessons", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LanguagePickerDialog(current: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Interface language") },
        text = {
            Column {
                AppLanguage.entries.forEach { lang ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(lang); onDismiss() }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = lang == current, onClick = { onSelect(lang); onDismiss() })
                        Text(lang.displayName)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
