package com.example.nuraienglish.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.CourseType
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType

@Composable
fun AdminScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Add Course", "Add Lesson", "Add Task")

    LaunchedEffect(state.successMessage, state.error) {
        if (state.successMessage != null || state.error != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        snackbarHost = {
            if (state.successMessage != null) {
                Snackbar { Text(state.successMessage!!) }
            }
            if (state.error != null) {
                Snackbar(containerColor = MaterialTheme.colorScheme.error) { Text(state.error!!) }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> AddCourseTab(isSaving = state.isSaving, onSave = { t, r, k, de, dr, dk, l, ty, lc, pt ->
                    viewModel.saveCourse("", t, r, k, de, dr, dk, l, ty, lc, pt)
                })
                1 -> AddLessonTab(
                    courses = state.courses.map { it.id to it.title(language) },
                    isSaving = state.isSaving,
                    onSave = { cid, te, tr, tk, ord, tc, pts ->
                        viewModel.saveLesson(cid, "", te, tr, tk, ord, tc, pts)
                    }
                )
                2 -> AddTaskTab(
                    courses = state.courses.map { it.id to it.title(language) },
                    isSaving = state.isSaving,
                    onSave = { cid, lid, task -> viewModel.saveTask(cid, lid, task) }
                )
            }
        }
    }
}

@Composable
private fun AddCourseTab(
    isSaving: Boolean,
    onSave: (String, String, String, String, String, String, String, CourseType, Int, Int) -> Unit
) {
    var titleEn by remember { mutableStateOf("") }
    var titleRu by remember { mutableStateOf("") }
    var titleKk by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var descRu by remember { mutableStateOf("") }
    var descKk by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("A1") }
    var type by remember { mutableStateOf(CourseType.VOCABULARY) }
    var lessonCount by remember { mutableStateOf("0") }
    var points by remember { mutableStateOf("0") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New Course", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        listOf("English title" to titleEn to { v: String -> titleEn = v },
               "Russian title" to titleRu to { v: String -> titleRu = v },
               "Kazakh title" to titleKk to { v: String -> titleKk = v },
               "English description" to descEn to { v: String -> descEn = v },
               "Russian description" to descRu to { v: String -> descRu = v },
               "Kazakh description" to descKk to { v: String -> descKk = v }
        ).forEach { (labelValue, onChange) ->
            OutlinedTextField(value = labelValue.second, onValueChange = onChange, label = { Text(labelValue.first) }, modifier = Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("Level (A1, A2…)") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = lessonCount, onValueChange = { lessonCount = it }, label = { Text("Lessons") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text("Points to unlock") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Type", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CourseType.entries.forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.name) })
            }
        }
        Button(
            onClick = { onSave(titleEn, titleRu, titleKk, descEn, descRu, descKk, level, type, lessonCount.toIntOrNull() ?: 0, points.toIntOrNull() ?: 0) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isSaving && titleEn.isNotBlank()
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Save Course")
        }
    }
}

@Composable
private fun AddLessonTab(
    courses: List<Pair<String, String>>,
    isSaving: Boolean,
    onSave: (String, String, String, String, Int, Int, Int) -> Unit
) {
    var selectedCourse by remember { mutableStateOf(courses.firstOrNull()?.first ?: "") }
    var titleEn by remember { mutableStateOf("") }
    var titleRu by remember { mutableStateOf("") }
    var titleKk by remember { mutableStateOf("") }
    var order by remember { mutableStateOf("1") }
    var taskCount by remember { mutableStateOf("5") }
    var points by remember { mutableStateOf("10") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New Lesson", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = courses.firstOrNull { it.first == selectedCourse }?.second ?: "Select course",
                onValueChange = {},
                readOnly = true,
                label = { Text("Course") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                courses.forEach { (id, title) ->
                    DropdownMenuItem(text = { Text(title) }, onClick = { selectedCourse = id; expanded = false })
                }
            }
        }

        OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("English title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = titleRu, onValueChange = { titleRu = it }, label = { Text("Russian title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = titleKk, onValueChange = { titleKk = it }, label = { Text("Kazakh title") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = order, onValueChange = { order = it }, label = { Text("Order") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = taskCount, onValueChange = { taskCount = it }, label = { Text("Task count") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text("Points") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        Button(
            onClick = { onSave(selectedCourse, titleEn, titleRu, titleKk, order.toIntOrNull() ?: 1, taskCount.toIntOrNull() ?: 5, points.toIntOrNull() ?: 10) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isSaving && titleEn.isNotBlank() && selectedCourse.isNotBlank()
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Save Lesson")
        }
    }
}

@Composable
private fun AddTaskTab(
    courses: List<Pair<String, String>>,
    isSaving: Boolean,
    onSave: (String, String, Task) -> Unit
) {
    var selectedCourse by remember { mutableStateOf(courses.firstOrNull()?.first ?: "") }
    var lessonId by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf(TaskType.WORD_TRANSLATION) }
    var questionEn by remember { mutableStateOf("") }
    var answerEn by remember { mutableStateOf("") }
    var answerRu by remember { mutableStateOf("") }
    var answerKk by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }
    var words by remember { mutableStateOf("") }
    var correctSentence by remember { mutableStateOf("") }
    var courseExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        ExposedDropdownMenuBox(expanded = courseExpanded, onExpandedChange = { courseExpanded = it }) {
            OutlinedTextField(
                value = courses.firstOrNull { it.first == selectedCourse }?.second ?: "Select course",
                onValueChange = {}, readOnly = true, label = { Text("Course") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(courseExpanded) }
            )
            ExposedDropdownMenu(expanded = courseExpanded, onDismissRequest = { courseExpanded = false }) {
                courses.forEach { (id, title) ->
                    DropdownMenuItem(text = { Text(title) }, onClick = { selectedCourse = id; courseExpanded = false })
                }
            }
        }

        OutlinedTextField(value = lessonId, onValueChange = { lessonId = it }, label = { Text("Lesson ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Text("Task type", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TaskType.entries.forEach { t ->
                FilterChip(selected = taskType == t, onClick = { taskType = t }, label = { Text(t.name.take(4)) })
            }
        }

        OutlinedTextField(value = questionEn, onValueChange = { questionEn = it }, label = { Text("Question (English)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = answerEn, onValueChange = { answerEn = it }, label = { Text("Answer (English)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = answerRu, onValueChange = { answerRu = it }, label = { Text("Answer (Russian)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = answerKk, onValueChange = { answerKk = it }, label = { Text("Answer (Kazakh)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        if (taskType == TaskType.MULTIPLE_CHOICE) {
            OutlinedTextField(value = options, onValueChange = { options = it }, label = { Text("Options (comma separated)") }, modifier = Modifier.fillMaxWidth())
        }
        if (taskType == TaskType.SENTENCE_BUILDING) {
            OutlinedTextField(value = words, onValueChange = { words = it }, label = { Text("Words (comma separated)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = correctSentence, onValueChange = { correctSentence = it }, label = { Text("Correct sentence") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }

        Button(
            onClick = {
                onSave(selectedCourse, lessonId, Task(
                    lessonId = lessonId, courseId = selectedCourse, type = taskType,
                    questionEn = questionEn, answerEn = answerEn, answerRu = answerRu, answerKk = answerKk,
                    options = options.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    words = words.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    correctSentence = correctSentence
                ))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isSaving && questionEn.isNotBlank() && lessonId.isNotBlank()
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Save Task")
        }
    }
}
