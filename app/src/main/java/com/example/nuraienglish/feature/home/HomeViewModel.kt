package com.example.nuraienglish.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.Progress
import com.example.nuraienglish.core.data.model.User
import com.example.nuraienglish.core.data.preferences.AppPreferences
import com.example.nuraienglish.core.data.repository.AuthRepository
import com.example.nuraienglish.core.data.repository.CourseRepository
import com.example.nuraienglish.core.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val courses: List<Course> = emptyList(),
    val progressMap: Map<String, Progress> = emptyMap(),
    val totalPoints: Int = 0,
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                courseRepository.observeCourses(),
                progressRepository.observeAllProgress()
            ) { courses, progressList ->
                val progressMap = progressList.associateBy { it.courseId }
                val totalPoints = progressList.sumOf { it.points }
                _state.value = _state.value.copy(
                    courses = courses,
                    progressMap = progressMap,
                    totalPoints = totalPoints,
                    isLoading = false
                )
            }.launchIn(this)

            authRepository.currentUser.collect { fbUser ->
                if (fbUser != null) {
                    val fullUser = authRepository.getUser(fbUser.uid) ?: fbUser
                    _state.value = _state.value.copy(user = fullUser, isAdmin = fullUser.isAdmin)
                }
            }
        }
        sync()
    }

    private fun sync() {
        viewModelScope.launch {
            runCatching { courseRepository.syncCourses() }
            runCatching { progressRepository.syncProgress() }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            val lang = com.example.nuraienglish.core.data.model.AppLanguage.entries
                .firstOrNull { it.code == code }
                ?: com.example.nuraienglish.core.data.model.AppLanguage.ENGLISH
            appPreferences.setLanguage(lang)
            val uid = authRepository.currentUser.firstOrNull()?.uid ?: return@launch
            authRepository.updateUserLanguage(uid, code)
        }
    }
}
