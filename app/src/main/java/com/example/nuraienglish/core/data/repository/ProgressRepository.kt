package com.example.nuraienglish.core.data.repository

import com.example.nuraienglish.core.data.local.dao.ProgressDao
import com.example.nuraienglish.core.data.local.entity.toDomain
import com.example.nuraienglish.core.data.local.entity.toEntity
import com.example.nuraienglish.core.data.model.Progress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val progressDao: ProgressDao
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    fun observeAllProgress(): Flow<List<Progress>> =
        progressDao.observeAll().map { it.map { e -> e.toDomain() } }

    suspend fun completeLesson(courseId: String, lessonId: String, points: Int) {
        val existing = progressDao.getByCourse(courseId)?.toDomain()
            ?: Progress(courseId = courseId)

        if (lessonId in existing.completedLessons) return

        val updated = existing.copy(
            completedLessons = existing.completedLessons + lessonId,
            points = existing.points + points,
            lastUpdated = System.currentTimeMillis()
        )
        progressDao.upsert(updated.toEntity())

        if (uid.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("progress").document(courseId)
                .set(updated.toFirestoreMap()).await()

            val userRef = firestore.collection("users").document(uid)
            firestore.runTransaction { tx ->
                val snap = tx.get(userRef)
                val currentPoints = snap.getLong("points")?.toInt() ?: 0
                tx.update(userRef, "points", currentPoints + points)
            }.await()
        }
    }

    suspend fun syncProgress() {
        if (uid.isBlank()) return
        val snap = firestore.collection("users").document(uid)
            .collection("progress").get().await()
        val progressList = snap.documents.mapNotNull { doc ->
            runCatching {
                val completed = (doc.get("completedLessons") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                Progress(
                    courseId = doc.id,
                    completedLessons = completed,
                    totalLessons = (doc.getLong("totalLessons") ?: 0).toInt(),
                    points = (doc.getLong("points") ?: 0).toInt(),
                    lastUpdated = doc.getLong("lastUpdated") ?: 0
                )
            }.getOrNull()
        }
        progressList.forEach { progressDao.upsert(it.toEntity()) }
    }

    private fun Progress.toFirestoreMap() = mapOf(
        "completedLessons" to completedLessons,
        "totalLessons" to totalLessons,
        "points" to points,
        "lastUpdated" to lastUpdated
    )
}
