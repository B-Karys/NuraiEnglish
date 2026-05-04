package com.example.nuraienglish.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Mistake
import com.example.nuraienglish.core.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReviewAnswerState { IDLE, CORRECT, WRONG }

data class ReviewUiState(
    val mistakes: List<Mistake> = emptyList(),
    val currentIndex: Int = 0,
    val answerState: ReviewAnswerState = ReviewAnswerState.IDLE,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val mistakes = reviewRepository.getMistakes()
            _state.value = ReviewUiState(mistakes = mistakes, isLoading = false, isFinished = mistakes.isEmpty())
        }
    }

    val currentMistake get() = _state.value.mistakes.getOrNull(_state.value.currentIndex)

    fun submitAnswer(answer: String, language: AppLanguage) {
        val mistake = currentMistake ?: return
        val correct = mistake.task.answer(language).trim().equals(answer.trim(), ignoreCase = true)
        _state.value = _state.value.copy(
            answerState = if (correct) ReviewAnswerState.CORRECT else ReviewAnswerState.WRONG
        )
    }

    fun next() {
        val s = _state.value
        val mistake = currentMistake
        if (s.answerState == ReviewAnswerState.CORRECT && mistake != null) {
            viewModelScope.launch { runCatching { reviewRepository.removeMistake(mistake.taskId) } }
        }
        val nextIndex = s.currentIndex + 1
        _state.value = if (nextIndex >= s.mistakes.size) {
            s.copy(isFinished = true, answerState = ReviewAnswerState.IDLE)
        } else {
            s.copy(currentIndex = nextIndex, answerState = ReviewAnswerState.IDLE)
        }
    }
}
