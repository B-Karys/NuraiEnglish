package com.example.nuraienglish.feature.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.LearningCard
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardStudyUiState(
    val learningCards: List<LearningCard> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
)

@HiltViewModel
class CardStudyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val courseId: String = savedStateHandle["courseId"] ?: ""
    private val lessonId: String = savedStateHandle["lessonId"] ?: ""

    private val _state = MutableStateFlow(CardStudyUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val learningCards = runCatching {
                courseRepository.getLearningCards(courseId, lessonId)
            }.getOrDefault(emptyList())
            val tasks = runCatching {
                courseRepository.getTasks(courseId, lessonId)
            }.getOrDefault(emptyList())
            _state.value = CardStudyUiState(
                learningCards = learningCards,
                tasks = tasks,
                isLoading = false,
            )
        }
    }

    fun toggleReveal() {
        _state.value = _state.value.copy(isRevealed = !_state.value.isRevealed)
    }

    fun reveal() {
        _state.value = _state.value.copy(isRevealed = true)
    }

    fun previous() {
        val state = _state.value
        if (state.currentIndex == 0) return
        _state.value = state.copy(
            currentIndex = state.currentIndex - 1,
            isRevealed = false,
            isFinished = false,
        )
    }

    fun next(cardCount: Int) {
        val state = _state.value
        val nextIndex = state.currentIndex + 1
        _state.value = if (nextIndex >= cardCount) {
            state.copy(isFinished = true, isRevealed = true)
        } else {
            state.copy(currentIndex = nextIndex, isRevealed = false)
        }
    }

    fun reviewAgain() {
        _state.value = _state.value.copy(
            currentIndex = 0,
            isRevealed = false,
            isFinished = false,
        )
    }
}
