package com.example.nuraienglish.core.data.model

data class Lesson(
    val id: String = "",
    val courseId: String = "",
    val titleEn: String = "",
    val titleRu: String = "",
    val titleKk: String = "",
    val order: Int = 0,
    val taskCount: Int = 0,
    val pointsReward: Int = 10,
    val isPublished: Boolean = true
) {
    fun title(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> titleRu.ifBlank { titleEn }
        AppLanguage.KAZAKH -> titleKk.ifBlank { titleEn }
        else -> titleEn
    }
}
