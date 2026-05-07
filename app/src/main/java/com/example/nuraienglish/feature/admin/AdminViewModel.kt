package com.example.nuraienglish.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.CourseType
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val courses: List<Course> = emptyList(),
    val lessonsByCourse: Map<String, List<Lesson>> = emptyMap(),
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            courseRepository.observeCourses().collect { courses ->
                _state.value = _state.value.copy(courses = courses)
                // Load lessons for every course so the task tab can show a dropdown
                courses.forEach { course ->
                    launch {
                        courseRepository.observeLessons(course.id).collect { lessons ->
                            val updated = _state.value.lessonsByCourse.toMutableMap()
                            updated[course.id] = lessons
                            _state.value = _state.value.copy(lessonsByCourse = updated)
                        }
                    }
                }
            }
        }
    }

    fun saveCourse(
        id: String, titleEn: String, titleRu: String, titleKk: String,
        descEn: String, descRu: String, descKk: String,
        level: String, type: CourseType, lessonCount: Int, pointsToUnlock: Int
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching {
                courseRepository.saveCourse(
                    Course(
                        id = id, titleEn = titleEn, titleRu = titleRu, titleKk = titleKk,
                        descriptionEn = descEn, descriptionRu = descRu, descriptionKk = descKk,
                        level = level, type = type, lessonCount = lessonCount, pointsToUnlock = pointsToUnlock
                    )
                )
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false, successMessage = "Course saved!")
                courseRepository.syncCourses()
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun saveLesson(courseId: String, id: String, titleEn: String, titleRu: String, titleKk: String, order: Int, taskCount: Int, points: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching {
                courseRepository.saveLesson(courseId, Lesson(id = id, courseId = courseId, titleEn = titleEn, titleRu = titleRu, titleKk = titleKk, order = order, taskCount = taskCount, pointsReward = points))
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false, successMessage = "Lesson saved!")
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun saveTask(courseId: String, lessonId: String, task: Task) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching { courseRepository.saveTask(courseId, lessonId, task) }
                .onSuccess { _state.value = _state.value.copy(isSaving = false, successMessage = "Task saved!") }
                .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(successMessage = null, error = null) }
}
