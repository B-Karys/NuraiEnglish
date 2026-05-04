package com.example.nuraienglish.feature.progress

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

data class ProgressUiState(
    val items: List<Pair<Course, Progress?>> = emptyList(),
    val totalPoints: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                courseRepository.observeCourses(),
                progressRepository.observeAllProgress()
            ) { courses, progressList ->
                val map = progressList.associateBy { it.courseId }
                _state.value = ProgressUiState(
                    items = courses.map { it to map[it.id] },
                    totalPoints = progressList.sumOf { it.points },
                    isLoading = false
                )
            }.launchIn(this)
        }
    }
}
