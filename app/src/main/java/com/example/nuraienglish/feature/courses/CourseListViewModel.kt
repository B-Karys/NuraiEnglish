package com.example.nuraienglish.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.Progress
import com.example.nuraienglish.core.data.repository.CourseRepository
import com.example.nuraienglish.core.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseListUiState(
    val courses: List<Course> = emptyList(),
    val progressMap: Map<String, Progress> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                courseRepository.observeCourses(),
                progressRepository.observeAllProgress()
            ) { courses, progressList ->
                _state.value = CourseListUiState(
                    courses = courses,
                    progressMap = progressList.associateBy { it.courseId },
                    isLoading = false
                )
            }.launchIn(this)
        }
    }
}
