package com.example.nuraienglish.core.data.model

data class Mistake(
    val taskId: String = "",
    val lessonId: String = "",
    val courseId: String = "",
    val mistakeCount: Int = 1,
    val lastMistakeAt: Long = System.currentTimeMillis(),
    val task: Task = Task()
)
