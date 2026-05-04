package com.example.nuraienglish.core.data.model

data class Task(
    val id: String = "",
    val lessonId: String = "",
    val courseId: String = "",
    val type: TaskType = TaskType.WORD_TRANSLATION,
    val order: Int = 0,
    val questionEn: String = "",
    val questionRu: String = "",
    val questionKk: String = "",
    val answerEn: String = "",
    val answerRu: String = "",
    val answerKk: String = "",
    val options: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val correctSentence: String = ""
) {
    fun question(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> questionRu.ifBlank { questionEn }
        AppLanguage.KAZAKH -> questionKk.ifBlank { questionEn }
        else -> questionEn
    }

    fun answer(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> answerRu.ifBlank { answerEn }
        AppLanguage.KAZAKH -> answerKk.ifBlank { answerEn }
        else -> answerEn
    }
}

enum class TaskType {
    WORD_TRANSLATION,
    SENTENCE_TRANSLATION,
    MULTIPLE_CHOICE,
    SENTENCE_BUILDING
}
