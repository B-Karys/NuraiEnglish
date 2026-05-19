package com.example.nuraienglish.feature.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.nativeLanguage
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.repository.CourseRepository
import com.example.nuraienglish.core.data.repository.ProgressRepository
import com.example.nuraienglish.core.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnswerState { IDLE, CORRECT, WRONG }

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val currentIndex: Int = 0,
    val answerState: AnswerState = AnswerState.IDLE,
    val correctCount: Int = 0,           // number of correct answers this session
    val lessonPointsAwarded: Int = 0,    // actual points given (0 if < 80% correct)
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val lessonPointsReward: Int = 10     // max reward if user passes
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val courseId: String = savedStateHandle["courseId"] ?: ""
    private val lessonId: String = savedStateHandle["lessonId"] ?: ""

    private val _state = MutableStateFlow(TaskUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val tasks = runCatching { courseRepository.getTasks(courseId, lessonId) }.getOrDefault(emptyList())
            _state.value = _state.value.copy(tasks = tasks.shuffled(), isLoading = false)
        }
    }

    val currentTask get() = _state.value.tasks.getOrNull(_state.value.currentIndex)

    fun submitAnswer(userAnswer: String, language: AppLanguage) {
        val task = currentTask ?: return
        // LISTEN_AND_WRITE: user types the English phrase they heard — compare against answerEn.
        // All other types: compare against the native-language answer (Russian/Kazakh).
        val correctAnswer = when (task.type) {
            com.example.nuraienglish.core.data.model.TaskType.LISTEN_AND_WRITE -> task.answerEn
            else -> task.answer(language.nativeLanguage())
        }
        val correct = correctAnswer.normalizeAnswer()
            .equals(userAnswer.normalizeAnswer(), ignoreCase = true)
        _state.value = _state.value.copy(answerState = if (correct) AnswerState.CORRECT else AnswerState.WRONG)
        if (!correct) {
            viewModelScope.launch { runCatching { reviewRepository.recordMistake(task) } }
        }
    }

    fun next(language: AppLanguage) {
        val s = _state.value
        val correct = if (s.answerState == AnswerState.CORRECT) s.correctCount + 1 else s.correctCount
        val nextIndex = s.currentIndex + 1
        if (nextIndex >= s.tasks.size) {
            // Award lesson points only if the user answered ≥ 80% correctly
            val passed = s.tasks.isNotEmpty() && correct.toFloat() / s.tasks.size >= 0.8f
            val reward = if (passed) s.lessonPointsReward else 0
            viewModelScope.launch {
                runCatching { progressRepository.completeLesson(courseId, lessonId, reward) }
            }
            _state.value = s.copy(
                isFinished = true,
                correctCount = correct,
                lessonPointsAwarded = reward,
                answerState = AnswerState.IDLE
            )
        } else {
            _state.value = s.copy(currentIndex = nextIndex, answerState = AnswerState.IDLE, correctCount = correct)
        }
    }
}

/** Strips trailing sentence-ending punctuation before comparing answers. */
private fun String.normalizeAnswer() = trim().trimEnd('.', '!', '?')
