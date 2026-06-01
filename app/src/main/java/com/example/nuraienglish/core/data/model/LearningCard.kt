package com.example.nuraienglish.core.data.model

data class LearningCard(
    val id: String = "",
    val courseId: String = "",
    val lessonId: String = "",
    val order: Int = 0,
    val type: LearningCardType = LearningCardType.VOCABULARY,
    val frontEn: String = "",
    val frontRu: String = "",
    val frontKk: String = "",
    val backEn: String = "",
    val backRu: String = "",
    val backKk: String = "",
    val noteEn: String = "",
    val noteRu: String = "",
    val noteKk: String = "",
    val speakText: String = "",
    val isPublished: Boolean = true,
) {
    fun front(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> frontRu.ifBlank { frontEn }
        AppLanguage.KAZAKH -> frontKk.ifBlank { frontEn }
        else -> frontEn
    }

    fun back(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> backRu.ifBlank { backEn }
        AppLanguage.KAZAKH -> backKk.ifBlank { backEn }
        else -> backEn
    }

    fun note(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> noteRu.ifBlank { noteEn }
        AppLanguage.KAZAKH -> noteKk.ifBlank { noteEn }
        else -> noteEn
    }
}

enum class LearningCardType {
    VOCABULARY,
    GRAMMAR,
    PHRASE,
    LISTENING,
    CUSTOM,
}
