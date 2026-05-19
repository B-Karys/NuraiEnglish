package com.example.nuraienglish.core.data.model

data class Course(
    val id: String = "",
    val titleEn: String = "",
    val titleRu: String = "",
    val titleKk: String = "",
    val descriptionEn: String = "",
    val descriptionRu: String = "",
    val descriptionKk: String = "",
    val level: String = "A1",
    val type: CourseType = CourseType.VOCABULARY,
    val order: Int = 0,
    val lessonCount: Int = 0,
    val pointsToUnlock: Int = 0,
    val isPublished: Boolean = true
) {
    fun title(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> titleRu.ifBlank { titleEn }
        AppLanguage.KAZAKH -> titleKk.ifBlank { titleEn }
        else -> titleEn
    }

    fun description(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> descriptionRu.ifBlank { descriptionEn }
        AppLanguage.KAZAKH -> descriptionKk.ifBlank { descriptionEn }
        else -> descriptionEn
    }
}

enum class CourseType { VOCABULARY, GRAMMAR, LISTENING }
