package com.example.nuraienglish.core.data.model

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    KAZAKH("kk", "Қазақша")
}

fun String.toAppLanguage(): AppLanguage =
    AppLanguage.entries.firstOrNull { it.code == this } ?: AppLanguage.ENGLISH

/**
 * Returns the language used for task answers.
 * English is the language being *learned*, so it is never the answer language.
 * If the UI is set to English, fall back to Russian.
 */
fun AppLanguage.nativeLanguage(): AppLanguage =
    if (this == AppLanguage.ENGLISH) AppLanguage.RUSSIAN else this
