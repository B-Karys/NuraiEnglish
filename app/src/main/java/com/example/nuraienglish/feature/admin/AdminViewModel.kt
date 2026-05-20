package com.example.nuraienglish.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.CourseType
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType
import com.example.nuraienglish.core.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AdminUiState(
    val courses: List<Course> = emptyList(),
    val lessonsByCourse: Map<String, List<Lesson>> = emptyMap(),
    val tasksByLesson: Map<String, List<Task>> = emptyMap(),
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            courseRepository.observeCourses().collect { courses ->
                _state.value = _state.value.copy(courses = courses)
                courses.forEach { course ->
                    launch {
                        courseRepository.observeLessons(course.id).collect { lessons ->
                            val updated = _state.value.lessonsByCourse.toMutableMap()
                            updated[course.id] = lessons
                            _state.value = _state.value.copy(lessonsByCourse = updated)
                        }
                    }
                }
            }
        }
    }

    fun saveCourse(
        id: String, titleEn: String, titleRu: String, titleKk: String,
        descEn: String, descRu: String, descKk: String,
        level: String, type: CourseType, lessonCount: Int, pointsToUnlock: Int,
        successMsg: String = "Saved!"
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching {
                courseRepository.saveCourse(
                    Course(
                        id = id, titleEn = titleEn, titleRu = titleRu, titleKk = titleKk,
                        descriptionEn = descEn, descriptionRu = descRu, descriptionKk = descKk,
                        level = level, type = type, lessonCount = lessonCount, pointsToUnlock = pointsToUnlock
                    )
                )
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false, successMessage = successMsg)
                courseRepository.syncCourses()
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun saveLesson(
        courseId: String, id: String,
        titleEn: String, titleRu: String, titleKk: String,
        order: Int, taskCount: Int, points: Int,
        successMsg: String = "Saved!"
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching {
                courseRepository.saveLesson(courseId, Lesson(
                    id = id, courseId = courseId,
                    titleEn = titleEn, titleRu = titleRu, titleKk = titleKk,
                    order = order, taskCount = taskCount, pointsReward = points
                ))
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false, successMessage = successMsg)
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun saveTask(courseId: String, lessonId: String, task: Task, successMsg: String = "Saved!") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching { courseRepository.saveTask(courseId, lessonId, task) }
                .onSuccess {
                    _state.value = _state.value.copy(isSaving = false, successMessage = successMsg)
                    // Refresh the task list for this lesson so edits appear immediately
                    loadTasks(courseId, lessonId)
                }
                .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
        }
    }

    fun loadTasks(courseId: String, lessonId: String) {
        if (lessonId.isBlank() || courseId.isBlank()) return
        viewModelScope.launch {
            val tasks = runCatching { courseRepository.getTasks(courseId, lessonId) }.getOrDefault(emptyList())
            _state.value = _state.value.copy(
                tasksByLesson = _state.value.tasksByLesson + (lessonId to tasks)
            )
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(successMessage = null, error = null) }

    // ─── Sample Data Seeding ──────────────────────────────────────────────────

    fun seedSampleData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching { doSeed() }
                .onSuccess {
                    _state.value = _state.value.copy(isSaving = false, successMessage = "Sample data created!")
                    courseRepository.syncCourses()
                }
                .onFailure {
                    _state.value = _state.value.copy(isSaving = false, error = "Seed failed: ${it.message}")
                }
        }
    }

    private suspend fun doSeed() {
        seedVocabulary()
        seedGrammar()
        seedListening()
    }

    // ── VOCABULARY ─────────────────────────────────────────────────────────────
    // Focus: translating English words into the native language.

    private suspend fun seedVocabulary() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A1", type = CourseType.VOCABULARY, lessonCount = 2, pointsToUnlock = 0,
            titleEn = "Basic Vocabulary", titleRu = "Базовая лексика", titleKk = "Негізгі сөздік",
            descriptionEn = "Learn the most essential English words for beginners.",
            descriptionRu = "Изучите самые важные слова английского языка для начинающих.",
            descriptionKk = "Бастаушыларға арналған ең маңызды ағылшын сөздерін үйреніңіз."
        ))

        // Lesson 1 — Greetings (5 word translations)
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 5, pointsReward = 15,
            titleEn = "Greetings", titleRu = "Приветствия", titleKk = "Амандасу"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Hello",
            questionRu = "Переведите слово: Hello",
            questionKk = "Сөзді аударыңыз: Hello",
            answerEn = "Hello", answerRu = "Привет", answerKk = "Сәлем"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Thank you",
            questionRu = "Переведите слово: Thank you",
            questionKk = "Сөзді аударыңыз: Thank you",
            answerEn = "Thank you", answerRu = "Спасибо", answerKk = "Рахмет"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Goodbye",
            questionRu = "Переведите слово: Goodbye",
            questionKk = "Сөзді аударыңыз: Goodbye",
            answerEn = "Goodbye", answerRu = "До свидания", answerKk = "Сау бол"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 4,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Please",
            questionRu = "Переведите слово: Please",
            questionKk = "Сөзді аударыңыз: Please",
            answerEn = "Please", answerRu = "Пожалуйста", answerKk = "Өтінем"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 5,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Sorry",
            questionRu = "Переведите слово: Sorry",
            questionKk = "Сөзді аударыңыз: Sorry",
            answerEn = "Sorry", answerRu = "Извините", answerKk = "Кешіріңіз"
        ))

        // Lesson 2 — Family (5 word translations)
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 5, pointsReward = 15,
            titleEn = "Family", titleRu = "Семья", titleKk = "Отбасы"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Mother",
            questionRu = "Переведите слово: Mother",
            questionKk = "Сөзді аударыңыз: Mother",
            answerEn = "Mother", answerRu = "Мама", answerKk = "Ана"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Father",
            questionRu = "Переведите слово: Father",
            questionKk = "Сөзді аударыңыз: Father",
            answerEn = "Father", answerRu = "Папа", answerKk = "Әке"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Sister",
            questionRu = "Переведите слово: Sister",
            questionKk = "Сөзді аударыңыз: Sister",
            answerEn = "Sister", answerRu = "Сестра", answerKk = "Сіңлі"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 4,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Brother",
            questionRu = "Переведите слово: Brother",
            questionKk = "Сөзді аударыңыз: Brother",
            answerEn = "Brother", answerRu = "Брат", answerKk = "Аға"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 5,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Son",
            questionRu = "Переведите слово: Son",
            questionKk = "Сөзді аударыңыз: Son",
            answerEn = "Son", answerRu = "Сын", answerKk = "Ұл"
        ))
    }

    // ── GRAMMAR ────────────────────────────────────────────────────────────────
    // Focus: grammar rules — fill-in-the-blank multiple choice + sentence building.

    private suspend fun seedGrammar() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A1", type = CourseType.GRAMMAR, lessonCount = 2, pointsToUnlock = 0,
            titleEn = "English Grammar Basics", titleRu = "Основы грамматики", titleKk = "Грамматика негіздері",
            descriptionEn = "Master the fundamental rules of English grammar.",
            descriptionRu = "Освойте основные правила английской грамматики.",
            descriptionKk = "Ағылшын грамматикасының негізгі ережелерін меңгеріңіз."
        ))

        // Lesson 1 — Present Simple (3 multiple-choice grammar + 2 sentence building)
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 5, pointsReward = 20,
            titleEn = "Present Simple", titleRu = "Настоящее простое время", titleKk = "Қазіргі қарапайым шақ"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct form: She ___ English.",
            questionRu = "Выберите правильную форму: She ___ English.",
            questionKk = "Дұрыс формасын таңдаңыз: She ___ English.",
            answerEn = "speaks", answerRu = "speaks", answerKk = "speaks",
            options = listOf("speaks", "speak", "speaking", "spoke"),
            optionsRu = listOf("speaks", "speak", "speaking", "spoke"),
            optionsKk = listOf("speaks", "speak", "speaking", "spoke")
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct form: They ___ to school.",
            questionRu = "Выберите правильную форму: They ___ to school.",
            questionKk = "Дұрыс формасын таңдаңыз: They ___ to school.",
            answerEn = "go", answerRu = "go", answerKk = "go",
            options = listOf("go", "goes", "going", "went"),
            optionsRu = listOf("go", "goes", "going", "went"),
            optionsKk = listOf("go", "goes", "going", "went")
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct form: He ___ a doctor.",
            questionRu = "Выберите правильную форму: He ___ a doctor.",
            questionKk = "Дұрыс формасын таңдаңыз: He ___ a doctor.",
            answerEn = "is", answerRu = "is", answerKk = "is",
            options = listOf("is", "are", "am", "be"),
            optionsRu = listOf("is", "are", "am", "be"),
            optionsKk = listOf("is", "are", "am", "be")
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 4,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Я хожу в школу каждый день",
            questionRu = "Составьте: «Я хожу в школу каждый день»",
            questionKk = "Құрастырыңыз: «Мен мектепке күн сайын барамын»",
            answerEn = "I go to school every day",
            answerRu = "I go to school every day",
            answerKk = "I go to school every day",
            words = listOf("I", "go", "to", "school", "every", "day", "home"),
            correctSentence = "I go to school every day"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 5,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Она читает книги дома",
            questionRu = "Составьте: «Она читает книги дома»",
            questionKk = "Құрастырыңыз: «Ол үйде кітап оқиды»",
            answerEn = "She reads books at home",
            answerRu = "She reads books at home",
            answerKk = "She reads books at home",
            words = listOf("She", "reads", "books", "at", "home", "school"),
            correctSentence = "She reads books at home"
        ))

        // Lesson 2 — Articles (3 multiple-choice + 2 sentence building)
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 5, pointsReward = 20,
            titleEn = "Articles: a, an, the", titleRu = "Артикли: a, an, the", titleKk = "Артикльдер: a, an, the"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct article: ___ apple",
            questionRu = "Выберите артикль: ___ apple",
            questionKk = "Артикльді таңдаңыз: ___ apple",
            answerEn = "an", answerRu = "an", answerKk = "an",
            options = listOf("an", "a", "the", "—"),
            optionsRu = listOf("an", "a", "the", "—"),
            optionsKk = listOf("an", "a", "the", "—")
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct article: ___ Sun",
            questionRu = "Выберите артикль: ___ Sun",
            questionKk = "Артикльді таңдаңыз: ___ Sun",
            answerEn = "the", answerRu = "the", answerKk = "the",
            options = listOf("the", "a", "an", "—"),
            optionsRu = listOf("the", "a", "an", "—"),
            optionsKk = listOf("the", "a", "an", "—")
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct article: ___ book",
            questionRu = "Выберите артикль: ___ book",
            questionKk = "Артикльді таңдаңыз: ___ book",
            answerEn = "a", answerRu = "a", answerKk = "a",
            options = listOf("a", "an", "the", "—"),
            optionsRu = listOf("a", "an", "the", "—"),
            optionsKk = listOf("a", "an", "the", "—")
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 4,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Собака очень большая",
            questionRu = "Составьте: «Собака очень большая»",
            questionKk = "Құрастырыңыз: «Ит өте үлкен»",
            answerEn = "The dog is very big",
            answerRu = "The dog is very big",
            answerKk = "The dog is very big",
            words = listOf("The", "dog", "is", "very", "big", "small"),
            correctSentence = "The dog is very big"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 5,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "У меня есть маленькая кошка",
            questionRu = "Составьте: «У меня есть маленькая кошка»",
            questionKk = "Құрастырыңыз: «Менің кішкентай мысығым бар»",
            answerEn = "I have a small cat",
            answerRu = "I have a small cat",
            answerKk = "I have a small cat",
            words = listOf("I", "have", "a", "small", "cat", "big"),
            correctSentence = "I have a small cat"
        ))
    }

    // ── LISTENING ──────────────────────────────────────────────────────────────
    // Focus: audio-only tasks — listen+translate and listen+write, no reading.

    private suspend fun seedListening() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A2", type = CourseType.LISTENING, lessonCount = 2, pointsToUnlock = 30,
            titleEn = "Listen & Understand", titleRu = "Слушай и понимай", titleKk = "Тыңда және түсін",
            descriptionEn = "Train your ear — listen to English and respond without reading the text.",
            descriptionRu = "Тренируйте слух — слушайте английскую речь и отвечайте, не читая текст.",
            descriptionKk = "Құлағыңызды жаттықтырыңыз — мәтінді оқымай ағылшынша тыңдап жауап беріңіз."
        ))

        // Lesson 1 — Common Phrases (3 listen+translate, 2 listen+write)
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 5, pointsReward = 20,
            titleEn = "Common Phrases", titleRu = "Общие фразы", titleKk = "Жалпы сөз тіркестері"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.LISTEN_AND_TRANSLATE,
            questionEn = "How are you",
            answerEn = "How are you", answerRu = "Как дела", answerKk = "Қалың қалай"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.LISTEN_AND_TRANSLATE,
            questionEn = "Nice to meet you",
            answerEn = "Nice to meet you", answerRu = "Приятно познакомиться", answerKk = "Танысқаныма қуаныштымын"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.LISTEN_AND_TRANSLATE,
            questionEn = "Excuse me",
            answerEn = "Excuse me", answerRu = "Простите", answerKk = "Кешіріңіз"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 4,
            type = TaskType.LISTEN_AND_WRITE,
            questionEn = "I am fine thank you",
            answerEn = "I am fine thank you", answerRu = "I am fine thank you", answerKk = "I am fine thank you"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 5,
            type = TaskType.LISTEN_AND_WRITE,
            questionEn = "See you later",
            answerEn = "See you later", answerRu = "See you later", answerKk = "See you later"
        ))

        // Lesson 2 — Days & Time (3 listen+translate, 2 listen+write)
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 5, pointsReward = 20,
            titleEn = "Days & Time", titleRu = "Дни и время", titleKk = "Күндер мен уақыт"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.LISTEN_AND_TRANSLATE,
            questionEn = "Today is Monday",
            answerEn = "Today is Monday", answerRu = "Сегодня понедельник", answerKk = "Бүгін дүйсенбі"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.LISTEN_AND_TRANSLATE,
            questionEn = "It is five o'clock",
            answerEn = "It is five o'clock", answerRu = "Сейчас пять часов", answerKk = "Сағат бес"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.LISTEN_AND_TRANSLATE,
            questionEn = "I wake up early",
            answerEn = "I wake up early", answerRu = "Я просыпаюсь рано", answerKk = "Мен ерте оянамын"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 4,
            type = TaskType.LISTEN_AND_WRITE,
            questionEn = "What time is it",
            answerEn = "What time is it", answerRu = "What time is it", answerKk = "What time is it"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 5,
            type = TaskType.LISTEN_AND_WRITE,
            questionEn = "See you on Friday",
            answerEn = "See you on Friday", answerRu = "See you on Friday", answerKk = "See you on Friday"
        ))
    }

    private fun uuid() = UUID.randomUUID().toString()
}
