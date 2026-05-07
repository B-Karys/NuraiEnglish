package com.example.nuraienglish.core.data.repository

import com.example.nuraienglish.core.data.local.dao.CourseDao
import com.example.nuraienglish.core.data.local.dao.LessonDao
import com.example.nuraienglish.core.data.local.entity.toDomain
import com.example.nuraienglish.core.data.local.entity.toEntity
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.CourseType
import com.example.nuraienglish.core.data.model.Lesson
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao
) {
    fun observeCourses(): Flow<List<Course>> =
        courseDao.observeAll().map { it.map { e -> e.toDomain() } }

    fun observeLessons(courseId: String): Flow<List<Lesson>> =
        lessonDao.observeByCourse(courseId).map { it.map { e -> e.toDomain() } }

    suspend fun syncCourses() {
        val snap = firestore.collection("courses")
            .whereEqualTo("isPublished", true)
            .get().await()
        val courses = snap.documents.mapNotNull { doc ->
            runCatching { doc.toObject(Course::class.java)?.copy(id = doc.id) }.getOrNull()
        }
        courseDao.upsertAll(courses.map { it.toEntity() })
    }

    suspend fun syncLessons(courseId: String) {
        val snap = firestore.collection("courses").document(courseId)
            .collection("lessons")
            .whereEqualTo("isPublished", true)
            .get().await()
        val lessons = snap.documents.mapNotNull { doc ->
            runCatching { doc.toObject(Lesson::class.java)?.copy(id = doc.id, courseId = courseId) }.getOrNull()
        }
        lessonDao.upsertAll(lessons.map { it.toEntity() })
    }

    suspend fun getTasks(courseId: String, lessonId: String): List<Task> {
        val snap = firestore.collection("courses").document(courseId)
            .collection("lessons").document(lessonId)
            .collection("tasks")
            .orderBy("order")
            .get().await()
        return snap.documents.mapNotNull { doc ->
            runCatching { doc.toObject(Task::class.java)?.copy(id = doc.id, lessonId = lessonId, courseId = courseId) }.getOrNull()
        }
    }

    suspend fun saveTask(courseId: String, lessonId: String, task: Task) {
        val ref = if (task.id.isBlank())
            firestore.collection("courses").document(courseId)
                .collection("lessons").document(lessonId)
                .collection("tasks").document()
        else
            firestore.collection("courses").document(courseId)
                .collection("lessons").document(lessonId)
                .collection("tasks").document(task.id)
        ref.set(task.toMap()).await()
    }

    suspend fun saveCourse(course: Course) {
        val id = course.id.ifBlank { UUID.randomUUID().toString() }
        val withId = course.copy(id = id)
        // Write to local Room so it appears immediately regardless of Firestore access
        courseDao.upsertAll(listOf(withId.toEntity()))
        // Best-effort sync to Firestore (fails silently on stub/restricted projects)
        runCatching {
            firestore.collection("courses").document(id).set(withId.toMap()).await()
        }
    }

    suspend fun saveLesson(courseId: String, lesson: Lesson) {
        val id = lesson.id.ifBlank { UUID.randomUUID().toString() }
        val withId = lesson.copy(id = id)
        // Write to local Room so it appears immediately regardless of Firestore access
        lessonDao.upsertAll(listOf(withId.toEntity()))
        // Best-effort sync to Firestore (fails silently on stub/restricted projects)
        runCatching {
            firestore.collection("courses").document(courseId)
                .collection("lessons").document(id).set(withId.toMap()).await()
        }
    }

    private fun Course.toMap() = mapOf(
        "titleEn" to titleEn, "titleRu" to titleRu, "titleKk" to titleKk,
        "descriptionEn" to descriptionEn, "descriptionRu" to descriptionRu, "descriptionKk" to descriptionKk,
        "level" to level, "type" to type.name, "order" to order,
        "lessonCount" to lessonCount, "pointsToUnlock" to pointsToUnlock, "isPublished" to isPublished
    )

    private fun Lesson.toMap() = mapOf(
        "courseId" to courseId, "titleEn" to titleEn, "titleRu" to titleRu, "titleKk" to titleKk,
        "order" to order, "taskCount" to taskCount, "pointsReward" to pointsReward, "isPublished" to isPublished
    )

    private fun Task.toMap() = mapOf(
        "lessonId" to lessonId, "courseId" to courseId, "type" to type.name, "order" to order,
        "questionEn" to questionEn, "questionRu" to questionRu, "questionKk" to questionKk,
        "answerEn" to answerEn, "answerRu" to answerRu, "answerKk" to answerKk,
        "options" to options, "words" to words, "correctSentence" to correctSentence
    )
}
