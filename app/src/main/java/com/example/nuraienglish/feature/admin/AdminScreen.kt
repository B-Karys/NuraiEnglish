package com.example.nuraienglish.feature.admin

import androidx.compose.foundation.horizontalScroll
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
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.CourseType
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType
import com.example.nuraienglish.core.ui.UiStrings
import com.example.nuraienglish.core.ui.uiStrings

@Composable
fun AdminScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val strings = language.uiStrings()
    val tabs = listOf(strings.adminAddCourse, strings.adminAddLesson, strings.adminAddTask, strings.adminSeedData)

    LaunchedEffect(state.successMessage, state.error) {
        if (state.successMessage != null || state.error != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.adminPanel, fontWeight = FontWeight.Bold) },
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
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> CourseTab(
                    strings = strings,
                    language = language,
                    courses = state.courses,
                    isSaving = state.isSaving,
                    onSave = { id, te, tr, tk, de, dr, dk, l, ty, lc, pt ->
                        viewModel.saveCourse(id, te, tr, tk, de, dr, dk, l, ty, lc, pt, strings.adminSaved)
                    }
                )
                1 -> LessonTab(
                    strings = strings,
                    language = language,
                    courses = state.courses,
                    lessonsByCourse = state.lessonsByCourse,
                    isSaving = state.isSaving,
                    onSave = { cid, id, te, tr, tk, ord, tc, pts ->
                        viewModel.saveLesson(cid, id, te, tr, tk, ord, tc, pts, strings.adminSaved)
                    }
                )
                2 -> TaskTab(
                    strings = strings,
                    language = language,
                    courses = state.courses,
                    lessonsByCourse = state.lessonsByCourse,
                    tasksByLesson = state.tasksByLesson,
                    isSaving = state.isSaving,
                    onLoadTasks = { cid, lid -> viewModel.loadTasks(cid, lid) },
                    onSave = { cid, lid, task ->
                        viewModel.saveTask(cid, lid, task, strings.adminSaved)
                    }
                )
                3 -> SeedDataTab(strings = strings, isSaving = state.isSaving, onSeed = viewModel::seedSampleData)
            }
        }
    }
}

// ─── Course Tab ───────────────────────────────────────────────────────────────

@Composable
private fun CourseTab(
    strings: UiStrings,
    language: AppLanguage,
    courses: List<Course>,
    isSaving: Boolean,
    onSave: (id: String, te: String, tr: String, tk: String, de: String, dr: String, dk: String, level: String, type: CourseType, lessonCount: Int, pointsToUnlock: Int) -> Unit
) {
    var editing by remember { mutableStateOf<Course?>(null) }
    var titleEn by remember(editing) { mutableStateOf(editing?.titleEn ?: "") }
    var titleRu by remember(editing) { mutableStateOf(editing?.titleRu ?: "") }
    var titleKk by remember(editing) { mutableStateOf(editing?.titleKk ?: "") }
    var descEn by remember(editing) { mutableStateOf(editing?.descriptionEn ?: "") }
    var descRu by remember(editing) { mutableStateOf(editing?.descriptionRu ?: "") }
    var descKk by remember(editing) { mutableStateOf(editing?.descriptionKk ?: "") }
    var level by remember(editing) { mutableStateOf(editing?.level ?: "A1") }
    var type by remember(editing) { mutableStateOf(editing?.type ?: CourseType.VOCABULARY) }
    var lessonCount by remember(editing) { mutableStateOf(editing?.lessonCount?.toString() ?: "0") }
    var points by remember(editing) { mutableStateOf(editing?.pointsToUnlock?.toString() ?: "0") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Existing courses list
        if (courses.isNotEmpty()) {
            SectionLabel(strings.courses)
            courses.forEach { course ->
                ExistingItemCard(
                    title = course.title(language),
                    subtitle = "${course.level} · ${courseTypeLabel(course.type, strings)}",
                    editLabel = strings.adminEdit,
                    onEdit = { editing = course }
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
        }

        // Form header
        Text(
            if (editing != null) strings.adminEditCourse else strings.adminNewCourse,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )

        OutlinedTextField(titleEn, { titleEn = it }, label = { Text("English title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(titleRu, { titleRu = it }, label = { Text("Russian title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(titleKk, { titleKk = it }, label = { Text("Kazakh title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(descEn, { descEn = it }, label = { Text("English description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(descRu, { descRu = it }, label = { Text("Russian description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(descKk, { descKk = it }, label = { Text("Kazakh description") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(level, { level = it }, label = { Text("Level") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(lessonCount, { lessonCount = it }, label = { Text("Lessons") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        OutlinedTextField(points, { points = it }, label = { Text("Points to unlock") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Text(strings.adminCourseType, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CourseType.entries.forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(courseTypeLabel(t, strings)) })
            }
        }

        SaveRow(
            strings = strings,
            isSaving = isSaving,
            isEditing = editing != null,
            saveLabel = strings.adminSaveCourse,
            enabled = titleEn.isNotBlank(),
            onCancel = { editing = null },
            onSave = {
                onSave(editing?.id ?: "", titleEn, titleRu, titleKk, descEn, descRu, descKk, level, type,
                    lessonCount.toIntOrNull() ?: 0, points.toIntOrNull() ?: 0)
                editing = null
            }
        )
    }
}

// ─── Lesson Tab ───────────────────────────────────────────────────────────────

@Composable
private fun LessonTab(
    strings: UiStrings,
    language: AppLanguage,
    courses: List<Course>,
    lessonsByCourse: Map<String, List<Lesson>>,
    isSaving: Boolean,
    onSave: (courseId: String, id: String, te: String, tr: String, tk: String, order: Int, taskCount: Int, points: Int) -> Unit
) {
    var selectedCourse by remember { mutableStateOf(courses.firstOrNull()?.id ?: "") }
    var courseExpanded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Lesson?>(null) }

    var titleEn by remember(editing) { mutableStateOf(editing?.titleEn ?: "") }
    var titleRu by remember(editing) { mutableStateOf(editing?.titleRu ?: "") }
    var titleKk by remember(editing) { mutableStateOf(editing?.titleKk ?: "") }
    var order by remember(editing) { mutableStateOf(editing?.order?.toString() ?: "1") }
    var taskCount by remember(editing) { mutableStateOf(editing?.taskCount?.toString() ?: "5") }
    var points by remember(editing) { mutableStateOf(editing?.pointsReward?.toString() ?: "10") }

    val lessons = lessonsByCourse[selectedCourse] ?: emptyList()
    LaunchedEffect(lessons) { if (editing != null && lessons.none { it.id == editing!!.id }) editing = null }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Course selector (always visible)
        CourseDropdown(
            courses = courses, selectedId = selectedCourse, language = language,
            expanded = courseExpanded, onExpandedChange = { courseExpanded = it },
            onSelect = { selectedCourse = it; courseExpanded = false; editing = null }
        )

        // Existing lessons for this course
        if (lessons.isNotEmpty()) {
            SectionLabel(strings.lessons)
            lessons.forEach { lesson ->
                ExistingItemCard(
                    title = lesson.title(language),
                    subtitle = "${strings.taskWord} ${lesson.taskCount} · ${lesson.pointsReward} ${strings.pts}",
                    editLabel = strings.adminEdit,
                    onEdit = { editing = lesson; selectedCourse = lesson.courseId }
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
        }

        Text(
            if (editing != null) strings.adminEditLesson else strings.adminNewLesson,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )

        OutlinedTextField(titleEn, { titleEn = it }, label = { Text("English title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(titleRu, { titleRu = it }, label = { Text("Russian title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(titleKk, { titleKk = it }, label = { Text("Kazakh title") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(order, { order = it }, label = { Text("Order") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(taskCount, { taskCount = it }, label = { Text("Tasks") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(points, { points = it }, label = { Text("Points") }, modifier = Modifier.weight(1f), singleLine = true)
        }

        SaveRow(
            strings = strings,
            isSaving = isSaving,
            isEditing = editing != null,
            saveLabel = strings.adminSaveLesson,
            enabled = titleEn.isNotBlank() && selectedCourse.isNotBlank(),
            onCancel = { editing = null },
            onSave = {
                onSave(selectedCourse, editing?.id ?: "", titleEn, titleRu, titleKk,
                    order.toIntOrNull() ?: 1, taskCount.toIntOrNull() ?: 5, points.toIntOrNull() ?: 10)
                editing = null
            }
        )
    }
}

// ─── Task Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun TaskTab(
    strings: UiStrings,
    language: AppLanguage,
    courses: List<Course>,
    lessonsByCourse: Map<String, List<Lesson>>,
    tasksByLesson: Map<String, List<Task>>,
    isSaving: Boolean,
    onLoadTasks: (courseId: String, lessonId: String) -> Unit,
    onSave: (courseId: String, lessonId: String, task: Task) -> Unit
) {
    var selectedCourse by remember { mutableStateOf(courses.firstOrNull()?.id ?: "") }
    var selectedLesson by remember { mutableStateOf("") }
    var courseExpanded by remember { mutableStateOf(false) }
    var lessonExpanded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Task?>(null) }

    var taskType by remember(editing) { mutableStateOf(editing?.type ?: TaskType.WORD_TRANSLATION) }
    var questionEn by remember(editing) { mutableStateOf(editing?.questionEn ?: "") }
    var questionRu by remember(editing) { mutableStateOf(editing?.questionRu ?: "") }
    var questionKk by remember(editing) { mutableStateOf(editing?.questionKk ?: "") }
    var answerEn by remember(editing) { mutableStateOf(editing?.answerEn ?: "") }
    var answerRu by remember(editing) { mutableStateOf(editing?.answerRu ?: "") }
    var answerKk by remember(editing) { mutableStateOf(editing?.answerKk ?: "") }
    var options by remember(editing) { mutableStateOf(editing?.options?.joinToString(", ") ?: "") }
    var words by remember(editing) { mutableStateOf(editing?.words?.joinToString(", ") ?: "") }
    var correctSentence by remember(editing) { mutableStateOf(editing?.correctSentence ?: "") }

    val lessons = lessonsByCourse[selectedCourse] ?: emptyList()
    val existingTasks = tasksByLesson[selectedLesson] ?: emptyList()

    // Sync selectedLesson when lessons load
    LaunchedEffect(selectedCourse) { selectedLesson = ""; editing = null }
    LaunchedEffect(lessons) {
        if (selectedLesson.isBlank()) selectedLesson = lessons.firstOrNull()?.id ?: ""
    }
    // Load tasks whenever lesson selection changes
    LaunchedEffect(selectedLesson) {
        if (selectedLesson.isNotBlank()) onLoadTasks(selectedCourse, selectedLesson)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Course dropdown
        CourseDropdown(
            courses = courses, selectedId = selectedCourse, language = language,
            expanded = courseExpanded, onExpandedChange = { courseExpanded = it },
            onSelect = { selectedCourse = it; courseExpanded = false }
        )

        // Lesson dropdown
        ExposedDropdownMenuBox(expanded = lessonExpanded, onExpandedChange = { lessonExpanded = it }) {
            OutlinedTextField(
                value = lessons.firstOrNull { it.id == selectedLesson }?.title(language)
                    ?: if (lessons.isEmpty()) strings.adminNone else "Select lesson",
                onValueChange = {}, readOnly = true, label = { Text("Lesson") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lessonExpanded) },
                enabled = lessons.isNotEmpty()
            )
            ExposedDropdownMenu(expanded = lessonExpanded, onDismissRequest = { lessonExpanded = false }) {
                lessons.forEach { lesson ->
                    DropdownMenuItem(
                        text = { Text(lesson.title(language)) },
                        onClick = { selectedLesson = lesson.id; lessonExpanded = false; editing = null }
                    )
                }
            }
        }

        // Existing tasks list
        if (existingTasks.isNotEmpty()) {
            SectionLabel(strings.tasks)
            existingTasks.forEach { task ->
                ExistingItemCard(
                    title = task.questionEn.take(50).let { if (task.questionEn.length > 50) "$it…" else it },
                    subtitle = task.type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                    editLabel = strings.adminEdit,
                    onEdit = { editing = task }
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
        }

        // Form
        Text(
            if (editing != null) strings.adminEditTask else strings.adminNewTask,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )

        Text(strings.adminTaskType, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskType.entries.forEach { t ->
                FilterChip(selected = taskType == t, onClick = { taskType = t }, label = { Text(taskTypeLabel(t, strings)) })
            }
        }

        OutlinedTextField(questionEn, { questionEn = it }, label = { Text("Question (English)") }, modifier = Modifier.fillMaxWidth())

        // Native-language question hints (for types that show them)
        if (taskType == TaskType.SENTENCE_BUILDING || taskType == TaskType.SENTENCE_TRANSLATION ||
            taskType == TaskType.WORD_TRANSLATION || taskType == TaskType.MULTIPLE_CHOICE) {
            OutlinedTextField(questionRu, { questionRu = it }, label = { Text("Question (Russian)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(questionKk, { questionKk = it }, label = { Text("Question (Kazakh)") }, modifier = Modifier.fillMaxWidth())
        }

        OutlinedTextField(answerEn, { answerEn = it }, label = { Text("Answer (English)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(answerRu, { answerRu = it }, label = { Text("Answer (Russian)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(answerKk, { answerKk = it }, label = { Text("Answer (Kazakh)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        if (taskType == TaskType.MULTIPLE_CHOICE) {
            OutlinedTextField(options, { options = it }, label = { Text("Options (comma separated)") }, modifier = Modifier.fillMaxWidth())
        }
        if (taskType == TaskType.SENTENCE_BUILDING) {
            OutlinedTextField(words, { words = it }, label = { Text("Words (comma separated)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(correctSentence, { correctSentence = it }, label = { Text("Correct sentence") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }

        SaveRow(
            strings = strings,
            isSaving = isSaving,
            isEditing = editing != null,
            saveLabel = strings.adminSaveTask,
            enabled = questionEn.isNotBlank() && selectedLesson.isNotBlank(),
            onCancel = { editing = null },
            onSave = {
                onSave(selectedCourse, selectedLesson, Task(
                    id = editing?.id ?: "",
                    lessonId = selectedLesson, courseId = selectedCourse,
                    type = taskType, order = editing?.order ?: (existingTasks.size + 1),
                    questionEn = questionEn, questionRu = questionRu, questionKk = questionKk,
                    answerEn = answerEn, answerRu = answerRu, answerKk = answerKk,
                    options = options.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    words = words.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    correctSentence = correctSentence
                ))
                editing = null
            }
        )
    }
}

// ─── Seed Data Tab ────────────────────────────────────────────────────────────

@Composable
private fun SeedDataTab(strings: UiStrings, isSaving: Boolean, onSeed: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(strings.adminSampleDataTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "3 ${strings.courses.lowercase()} · 2 ${strings.lessons.lowercase()} · 5 ${strings.tasks.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SeedRow("📖", "Vocabulary A1", "2 ${strings.lessons.lowercase()} · ${strings.typeVocabulary.lowercase()}")
                SeedRow("📝", "Grammar A1",    "2 ${strings.lessons.lowercase()} · ${strings.typeGrammar.lowercase()}")
                SeedRow("👂", "Listening A2",  "2 ${strings.lessons.lowercase()} · ${strings.typeListening.lowercase()}")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSeed,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(strings.adminCreating)
            } else {
                Text(strings.adminCreateSampleData, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ExistingItemCard(
    title: String,
    subtitle: String,
    editLabel: String,
    onEdit: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onEdit) { Text(editLabel) }
        }
    }
}

@Composable
private fun SaveRow(
    strings: UiStrings,
    isSaving: Boolean,
    isEditing: Boolean,
    saveLabel: String,
    enabled: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isEditing) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp)) {
                Text(strings.adminCancel)
            }
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(if (isEditing) 2f else 1f).height(50.dp),
            enabled = !isSaving && enabled
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(saveLabel)
        }
    }
}

@Composable
private fun CourseDropdown(
    courses: List<Course>,
    selectedId: String,
    language: AppLanguage,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = courses.firstOrNull { it.id == selectedId }?.title(language) ?: "Select course",
            onValueChange = {}, readOnly = true, label = { Text("Course") },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            courses.forEach { course ->
                DropdownMenuItem(text = { Text(course.title(language)) }, onClick = { onSelect(course.id) })
            }
        }
    }
}

private fun courseTypeLabel(type: CourseType, strings: UiStrings) = when (type) {
    CourseType.VOCABULARY -> strings.typeVocabulary
    CourseType.GRAMMAR    -> strings.typeGrammar
    CourseType.LISTENING  -> strings.typeListening
}

private fun taskTypeLabel(type: TaskType, strings: UiStrings) = when (type) {
    TaskType.WORD_TRANSLATION     -> "Word"
    TaskType.SENTENCE_TRANSLATION -> "Sentence"
    TaskType.MULTIPLE_CHOICE      -> "Multiple choice"
    TaskType.SENTENCE_BUILDING    -> "Build"
    TaskType.LISTEN_AND_TRANSLATE -> strings.listenAndTranslate
    TaskType.LISTEN_AND_WRITE     -> strings.listenAndWrite
}

@Composable
private fun SeedRow(emoji: String, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
