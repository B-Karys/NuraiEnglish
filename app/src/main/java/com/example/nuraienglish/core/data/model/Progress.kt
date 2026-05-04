package com.example.nuraienglish.core.data.model

data class Progress(
    val courseId: String = "",
    val completedLessons: List<String> = emptyList(),
    val totalLessons: Int = 0,
    val points: Int = 0,
    val lastUpdated: Long = 0L
) {
    val completionFraction: Float
        get() = if (totalLessons == 0) 0f else completedLessons.size.toFloat() / totalLessons
}
