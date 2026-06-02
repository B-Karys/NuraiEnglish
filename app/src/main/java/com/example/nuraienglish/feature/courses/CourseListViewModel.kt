package com.example.nuraienglish.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.Progress
import com.example.nuraienglish.core.data.repository.AuthRepository
import com.example.nuraienglish.core.data.repository.CourseRepository
import com.example.nuraienglish.core.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseListUiState(
    val courses: List<Course> = emptyList(),
    val progressMap: Map<String, Progress> = emptyMap(),
    val totalPoints: Int = 0,
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    companion object {
        private val ADMIN_EMAILS = setOf(
            "shayne.f@mail.ru"
        )
    }

    private val _state = MutableStateFlow(CourseListUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                courseRepository.observeCourses(),
                progressRepository.observeAllProgress(),
                authRepository.currentUser
            ) { courses, progressList, fbUser ->
                val fullUser = fbUser?.let { authRepository.getUser(it.uid) ?: it }
                _state.value = CourseListUiState(
                    courses = courses,
                    progressMap = progressList.associateBy { it.courseId },
                    totalPoints = progressList.sumOf { it.points },
                    isAdmin = fullUser?.isAdmin == true || ADMIN_EMAILS.contains(fullUser?.email),
                    isLoading = false
                )
            }.launchIn(this)
        }
    }
}
