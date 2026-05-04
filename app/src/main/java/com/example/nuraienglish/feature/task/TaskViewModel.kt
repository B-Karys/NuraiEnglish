package com.example.nuraienglish.feature.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.AppLanguage
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
    val earnedPoints: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val lessonPointsReward: Int = 10
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
        val correct = task.answer(language).trim().equals(userAnswer.trim(), ignoreCase = true)
        _state.value = _state.value.copy(answerState = if (correct) AnswerState.CORRECT else AnswerState.WRONG)
        if (!correct) {
            viewModelScope.launch { runCatching { reviewRepository.recordMistake(task) } }
        }
    }

    fun next(language: AppLanguage) {
        val s = _state.value
        val points = if (s.answerState == AnswerState.CORRECT) s.earnedPoints + 1 else s.earnedPoints
        val nextIndex = s.currentIndex + 1
        if (nextIndex >= s.tasks.size) {
            viewModelScope.launch {
                runCatching { progressRepository.completeLesson(courseId, lessonId, s.lessonPointsReward) }
            }
            _state.value = s.copy(isFinished = true, earnedPoints = points, answerState = AnswerState.IDLE)
        } else {
            _state.value = s.copy(currentIndex = nextIndex, answerState = AnswerState.IDLE, earnedPoints = points)
        }
    }
}
