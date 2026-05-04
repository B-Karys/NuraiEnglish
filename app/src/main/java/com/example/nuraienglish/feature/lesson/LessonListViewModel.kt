package com.example.nuraienglish.feature.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.data.model.Progress
import com.example.nuraienglish.core.data.repository.CourseRepository
import com.example.nuraienglish.core.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonListUiState(
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList(),
    val progress: Progress? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class LessonListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val courseId: String = savedStateHandle["courseId"] ?: ""
    private val _state = MutableStateFlow(LessonListUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                courseRepository.observeLessons(courseId),
                progressRepository.observeAllProgress()
            ) { lessons, progressList ->
                _state.value = _state.value.copy(
                    lessons = lessons,
                    progress = progressList.firstOrNull { it.courseId == courseId },
                    isLoading = false
                )
            }.launchIn(this)
        }
        viewModelScope.launch {
            runCatching { courseRepository.syncLessons(courseId) }
        }
    }
}
