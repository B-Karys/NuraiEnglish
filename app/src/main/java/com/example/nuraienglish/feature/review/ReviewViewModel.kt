package com.example.nuraienglish.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.core.data.model.Mistake
import com.example.nuraienglish.core.data.model.nativeLanguage
import com.example.nuraienglish.core.data.repository.ReviewRepository
import com.example.nuraienglish.feature.task.AnswerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val mistakes: List<Mistake> = emptyList(),
    val currentIndex: Int = 0,
    val answerState: AnswerState = AnswerState.IDLE,
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
        // LISTEN_AND_WRITE: compare against English; all others: native-language answer.
        val correctAnswer = when (mistake.task.type) {
            com.example.nuraienglish.core.data.model.TaskType.LISTEN_AND_WRITE -> mistake.task.answerEn
            else -> mistake.task.answer(language.nativeLanguage())
        }
        val correct = correctAnswer.normalizeAnswer()
            .equals(answer.normalizeAnswer(), ignoreCase = true)
        _state.value = _state.value.copy(
            answerState = if (correct) AnswerState.CORRECT else AnswerState.WRONG
        )
    }

    fun next() {
        val s = _state.value
        val mistake = currentMistake
        if (s.answerState == AnswerState.CORRECT && mistake != null) {
            viewModelScope.launch { runCatching { reviewRepository.removeMistake(mistake.taskId) } }
        }
        val nextIndex = s.currentIndex + 1
        _state.value = if (nextIndex >= s.mistakes.size) {
            s.copy(isFinished = true, answerState = AnswerState.IDLE)
        } else {
            s.copy(currentIndex = nextIndex, answerState = AnswerState.IDLE)
        }
    }
}

private fun String.normalizeAnswer() = trim().trimEnd('.', '!', '?')
