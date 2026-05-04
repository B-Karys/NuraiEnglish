package com.example.nuraienglish.core.data.repository

import com.example.nuraienglish.core.data.model.Mistake
import com.example.nuraienglish.core.data.model.Task
import com.example.nuraienglish.core.data.model.TaskType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    suspend fun getMistakes(): List<Mistake> {
        if (uid.isBlank()) return emptyList()
        return runCatching {
            val snap = firestore.collection("users").document(uid)
                .collection("mistakes").get().await()
            snap.documents.mapNotNull { doc ->
                val taskMap = doc.get("task") as? Map<*, *> ?: return@mapNotNull null
                val task = Task(
                    id = taskMap["id"] as? String ?: "",
                    lessonId = taskMap["lessonId"] as? String ?: "",
                    courseId = taskMap["courseId"] as? String ?: "",
                    type = runCatching {
                        TaskType.valueOf(taskMap["type"] as? String ?: "")
                    }.getOrDefault(TaskType.WORD_TRANSLATION),
                    questionEn = taskMap["questionEn"] as? String ?: "",
                    questionRu = taskMap["questionRu"] as? String ?: "",
                    questionKk = taskMap["questionKk"] as? String ?: "",
                    answerEn = taskMap["answerEn"] as? String ?: "",
                    answerRu = taskMap["answerRu"] as? String ?: "",
                    answerKk = taskMap["answerKk"] as? String ?: "",
                    options = (taskMap["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    words = (taskMap["words"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    correctSentence = taskMap["correctSentence"] as? String ?: ""
                )
                Mistake(
                    taskId = doc.id,
                    lessonId = doc.getString("lessonId") ?: "",
                    courseId = doc.getString("courseId") ?: "",
                    mistakeCount = (doc.getLong("mistakeCount") ?: 1).toInt(),
                    lastMistakeAt = doc.getLong("lastMistakeAt") ?: 0L,
                    task = task
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun recordMistake(task: Task) {
        if (uid.isBlank()) return
        val ref = firestore.collection("users").document(uid)
            .collection("mistakes").document(task.id)
        val snap = ref.get().await()
        val currentCount = snap.getLong("mistakeCount")?.toInt() ?: 0
        ref.set(mapOf(
            "lessonId" to task.lessonId,
            "courseId" to task.courseId,
            "mistakeCount" to currentCount + 1,
            "lastMistakeAt" to System.currentTimeMillis(),
            "task" to task.toMap()
        )).await()
    }

    suspend fun removeMistake(taskId: String) {
        if (uid.isBlank()) return
        firestore.collection("users").document(uid)
            .collection("mistakes").document(taskId).delete().await()
    }

    private fun Task.toMap() = mapOf(
        "id" to id, "lessonId" to lessonId, "courseId" to courseId,
        "type" to type.name, "order" to order,
        "questionEn" to questionEn, "questionRu" to questionRu, "questionKk" to questionKk,
        "answerEn" to answerEn, "answerRu" to answerRu, "answerKk" to answerKk,
        "options" to options, "words" to words, "correctSentence" to correctSentence
    )
}
