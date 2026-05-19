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
    // Multiple-choice options per language.
    // optionsEn = the base list (used when no language-specific list is set).
    val options: List<String> = emptyList(),    // kept for backwards-compat / English options
    val optionsRu: List<String> = emptyList(),
    val optionsKk: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val correctSentence: String = ""
) {
    fun question(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> questionRu.ifBlank { questionEn }
        AppLanguage.KAZAKH  -> questionKk.ifBlank { questionEn }
        else                -> questionEn
    }

    fun answer(language: AppLanguage) = when (language) {
        AppLanguage.RUSSIAN -> answerRu.ifBlank { answerEn }
        AppLanguage.KAZAKH  -> answerKk.ifBlank { answerEn }
        else                -> answerEn
    }

    /** Returns the options list for the given language, falling back to the base list. */
    fun options(language: AppLanguage): List<String> = when (language) {
        AppLanguage.RUSSIAN -> optionsRu.ifEmpty { options }
        AppLanguage.KAZAKH  -> optionsKk.ifEmpty { options }
        else                -> options
    }
}

enum class TaskType {
    WORD_TRANSLATION,
    SENTENCE_TRANSLATION,
    MULTIPLE_CHOICE,
    SENTENCE_BUILDING,
    /** User listens to the English audio and translates it into their native language. */
    LISTEN_AND_TRANSLATE,
    /** User listens to the English audio and types exactly what they hear (English dictation). */
    LISTEN_AND_WRITE
}
