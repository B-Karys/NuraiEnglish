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
        level: String, type: CourseType, lessonCount: Int, pointsToUnlock: Int
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
                _state.value = _state.value.copy(isSaving = false, successMessage = "Course saved!")
                courseRepository.syncCourses()
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun saveLesson(courseId: String, id: String, titleEn: String, titleRu: String, titleKk: String, order: Int, taskCount: Int, points: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching {
                courseRepository.saveLesson(courseId, Lesson(id = id, courseId = courseId, titleEn = titleEn, titleRu = titleRu, titleKk = titleKk, order = order, taskCount = taskCount, pointsReward = points))
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false, successMessage = "Lesson saved!")
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun saveTask(courseId: String, lessonId: String, task: Task) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching { courseRepository.saveTask(courseId, lessonId, task) }
                .onSuccess { _state.value = _state.value.copy(isSaving = false, successMessage = "Task saved!") }
                .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
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
        seedSpeaking()
    }

    // ── VOCABULARY ─────────────────────────────────────────────────────────────

    private suspend fun seedVocabulary() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A1", type = CourseType.VOCABULARY, lessonCount = 2, pointsToUnlock = 0,
            titleEn = "Basic Vocabulary", titleRu = "Базовая лексика", titleKk = "Негізгі сөздік",
            descriptionEn = "Learn the most essential English words for beginners.",
            descriptionRu = "Изучите самые важные слова английского языка для начинающих.",
            descriptionKk = "Бастаушыларға арналған ең маңызды ағылшын сөздерін үйреніңіз."
        ))

        // Lesson 1 — Greetings
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 3, pointsReward = 15,
            titleEn = "Greetings", titleRu = "Приветствия", titleKk = "Амандасу"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Hello", questionRu = "Переведите слово: Hello", questionKk = "Сөзді аударыңыз: Hello",
            answerEn = "Hello", answerRu = "Привет", answerKk = "Сәлем"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Thank you", questionRu = "Переведите: Thank you", questionKk = "Аударыңыз: Thank you",
            answerEn = "Thank you", answerRu = "Спасибо", answerKk = "Рахмет"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "What does 'Goodbye' mean?", questionRu = "Что значит 'Goodbye'?", questionKk = "'Goodbye' нені білдіреді?",
            answerEn = "Goodbye", answerRu = "До свидания", answerKk = "Сау бол",
            options = listOf("До свидания", "Привет", "Спасибо", "Пожалуйста")
        ))

        // Lesson 2 — Family
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 3, pointsReward = 15,
            titleEn = "Family", titleRu = "Семья", titleKk = "Отбасы"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Mother", questionRu = "Переведите: Mother", questionKk = "Аударыңыз: Mother",
            answerEn = "Mother", answerRu = "Мама", answerKk = "Ана"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Brother", questionRu = "Переведите: Brother", questionKk = "Аударыңыз: Brother",
            answerEn = "Brother", answerRu = "Брат", answerKk = "Аға"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Which word means 'Sister'?", questionRu = "Какое слово означает 'Сестра'?", questionKk = "Қайсы сөз 'Сіңлі' дегенді білдіреді?",
            answerEn = "Sister", answerRu = "Сестра", answerKk = "Сіңлі",
            options = listOf("Sister", "Brother", "Mother", "Father")
        ))
    }

    // ── GRAMMAR ────────────────────────────────────────────────────────────────

    private suspend fun seedGrammar() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A1", type = CourseType.GRAMMAR, lessonCount = 2, pointsToUnlock = 0,
            titleEn = "English Grammar Basics", titleRu = "Основы грамматики", titleKk = "Грамматика негіздері",
            descriptionEn = "Master the fundamental rules of English grammar.",
            descriptionRu = "Освойте основные правила английской грамматики.",
            descriptionKk = "Ағылшын грамматикасының негізгі ережелерін меңгеріңіз."
        ))

        // Lesson 1 — Present Simple
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 3, pointsReward = 20,
            titleEn = "Present Simple", titleRu = "Настоящее простое время", titleKk = "Қазіргі қарапайым шақ"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.SENTENCE_TRANSLATION,
            questionEn = "I am a student.", questionRu = "Переведите: I am a student.", questionKk = "Аударыңыз: I am a student.",
            answerEn = "I am a student.", answerRu = "Я студент.", answerKk = "Мен студентпін."
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct form: She ___ English.", questionRu = "Выберите правильную форму: She ___ English.", questionKk = "Дұрыс формасын таңдаңыз: She ___ English.",
            answerEn = "speaks", answerRu = "speaks", answerKk = "speaks",
            options = listOf("speaks", "speak", "speaking", "spoke")
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Build the sentence:", questionRu = "Составьте предложение:", questionKk = "Сөйлем құрыңыз:",
            answerEn = "I go to school every day", answerRu = "I go to school every day", answerKk = "I go to school every day",
            words = listOf("I", "go", "to", "school", "every", "day"),
            correctSentence = "I go to school every day"
        ))

        // Lesson 2 — Articles
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 3, pointsReward = 20,
            titleEn = "Articles: a, an, the", titleRu = "Артикли: a, an, the", titleKk = "Артикльдер: a, an, the"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Choose the correct article: ___ apple", questionRu = "Выберите артикль: ___ apple", questionKk = "Артикльді таңдаңыз: ___ apple",
            answerEn = "an", answerRu = "an", answerKk = "an",
            options = listOf("an", "a", "the", "—")
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.SENTENCE_TRANSLATION,
            questionEn = "The cat is on the table.", questionRu = "Переведите: The cat is on the table.", questionKk = "Аударыңыз: The cat is on the table.",
            answerEn = "The cat is on the table.", answerRu = "Кошка на столе.", answerKk = "Мысық үстелде."
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Build the sentence:", questionRu = "Составьте предложение:", questionKk = "Сөйлем құрыңыз:",
            answerEn = "The dog is very big", answerRu = "The dog is very big", answerKk = "The dog is very big",
            words = listOf("The", "dog", "is", "very", "big"),
            correctSentence = "The dog is very big"
        ))
    }

    // ── LISTENING ──────────────────────────────────────────────────────────────

    private suspend fun seedListening() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A2", type = CourseType.LISTENING, lessonCount = 2, pointsToUnlock = 30,
            titleEn = "Listen & Understand", titleRu = "Слушай и понимай", titleKk = "Тыңда және түсін",
            descriptionEn = "Improve your English listening and comprehension skills.",
            descriptionRu = "Развивайте навыки понимания английской речи на слух.",
            descriptionKk = "Ағылшын тілін тыңдап түсіну дағдыларыңызды дамытыңыз."
        ))

        // Lesson 1 — Common Phrases
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 3, pointsReward = 20,
            titleEn = "Common Phrases", titleRu = "Общие фразы", titleKk = "Жалпы сөз тіркестері"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.SENTENCE_TRANSLATION,
            questionEn = "How are you?", questionRu = "Переведите: How are you?", questionKk = "Аударыңыз: How are you?",
            answerEn = "How are you?", answerRu = "Как дела?", answerKk = "Қалың қалай?"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Please", questionRu = "Переведите: Please", questionKk = "Аударыңыз: Please",
            answerEn = "Please", answerRu = "Пожалуйста", answerKk = "Өтінем"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "What does 'Excuse me' mean?", questionRu = "Что значит 'Excuse me'?", questionKk = "'Excuse me' нені білдіреді?",
            answerEn = "Excuse me", answerRu = "Извините", answerKk = "Кешіріңіз",
            options = listOf("Извините", "Спасибо", "Пожалуйста", "Привет")
        ))

        // Lesson 2 — Days & Time
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 3, pointsReward = 20,
            titleEn = "Days & Time", titleRu = "Дни и время", titleKk = "Күндер мен уақыт"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.WORD_TRANSLATION,
            questionEn = "Monday", questionRu = "Переведите: Monday", questionKk = "Аударыңыз: Monday",
            answerEn = "Monday", answerRu = "Понедельник", answerKk = "Дүйсенбі"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "Which day comes after Tuesday?", questionRu = "Какой день идёт после вторника?", questionKk = "Сейсенбіден кейін қандай күн келеді?",
            answerEn = "Wednesday", answerRu = "Среда", answerKk = "Сәрсенбі",
            options = listOf("Wednesday", "Monday", "Friday", "Sunday")
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.SENTENCE_TRANSLATION,
            questionEn = "It is five o'clock.", questionRu = "Переведите: It is five o'clock.", questionKk = "Аударыңыз: It is five o'clock.",
            answerEn = "It is five o'clock.", answerRu = "Сейчас пять часов.", answerKk = "Сағат бес."
        ))
    }

    // ── SPEAKING ───────────────────────────────────────────────────────────────

    private suspend fun seedSpeaking() {
        val cid = uuid()
        courseRepository.saveCourse(Course(
            id = cid, level = "A2", type = CourseType.SPEAKING, lessonCount = 2, pointsToUnlock = 30,
            titleEn = "Speak English Confidently", titleRu = "Говори по-английски уверенно", titleKk = "Ағылшынша сенімді сөйле",
            descriptionEn = "Practice speaking English with everyday expressions.",
            descriptionRu = "Практикуйте разговорный английский с повседневными выражениями.",
            descriptionKk = "Күнделікті сөз тіркестерімен ағылшынша сөйлеуді жаттығыңыз."
        ))

        // Lesson 1 — Introductions
        val l1 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l1, courseId = cid, order = 1, taskCount = 3, pointsReward = 25,
            titleEn = "Introducing Yourself", titleRu = "Представление", titleKk = "Өзіңді таныстыру"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 1,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Build the sentence:", questionRu = "Составьте предложение:", questionKk = "Сөйлем құрыңыз:",
            answerEn = "My name is Alex", answerRu = "My name is Alex", answerKk = "My name is Alex",
            words = listOf("My", "name", "is", "Alex", "am", "I"),
            correctSentence = "My name is Alex"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 2,
            type = TaskType.SENTENCE_TRANSLATION,
            questionEn = "Nice to meet you!", questionRu = "Переведите: Nice to meet you!", questionKk = "Аударыңыз: Nice to meet you!",
            answerEn = "Nice to meet you!", answerRu = "Приятно познакомиться!", answerKk = "Танысқаныма қуаныштымын!"
        ))
        courseRepository.saveTask(cid, l1, Task(courseId = cid, lessonId = l1, order = 3,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Build the sentence:", questionRu = "Составьте предложение:", questionKk = "Сөйлем құрыңыз:",
            answerEn = "I am from Kazakhstan", answerRu = "I am from Kazakhstan", answerKk = "I am from Kazakhstan",
            words = listOf("I", "am", "from", "Kazakhstan", "live", "in"),
            correctSentence = "I am from Kazakhstan"
        ))

        // Lesson 2 — Daily Routine
        val l2 = uuid()
        courseRepository.saveLesson(cid, Lesson(
            id = l2, courseId = cid, order = 2, taskCount = 3, pointsReward = 25,
            titleEn = "Daily Routine", titleRu = "Распорядок дня", titleKk = "Күнделікті тәртіп"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 1,
            type = TaskType.SENTENCE_TRANSLATION,
            questionEn = "I wake up at seven.", questionRu = "Переведите: I wake up at seven.", questionKk = "Аударыңыз: I wake up at seven.",
            answerEn = "I wake up at seven.", answerRu = "Я просыпаюсь в семь.", answerKk = "Мен жетіде оянамын."
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 2,
            type = TaskType.SENTENCE_BUILDING,
            questionEn = "Build the sentence:", questionRu = "Составьте предложение:", questionKk = "Сөйлем құрыңыз:",
            answerEn = "I eat breakfast every morning", answerRu = "I eat breakfast every morning", answerKk = "I eat breakfast every morning",
            words = listOf("I", "eat", "breakfast", "every", "morning", "dinner"),
            correctSentence = "I eat breakfast every morning"
        ))
        courseRepository.saveTask(cid, l2, Task(courseId = cid, lessonId = l2, order = 3,
            type = TaskType.MULTIPLE_CHOICE,
            questionEn = "What does 'I go to bed' mean?", questionRu = "Что значит 'I go to bed'?", questionKk = "'I go to bed' нені білдіреді?",
            answerEn = "I go to bed", answerRu = "Я ложусь спать", answerKk = "Мен ұйқыға жатамын",
            options = listOf("Я ложусь спать", "Я встаю", "Я завтракаю", "Я иду в школу")
        ))
    }

    private fun uuid() = UUID.randomUUID().toString()
}
