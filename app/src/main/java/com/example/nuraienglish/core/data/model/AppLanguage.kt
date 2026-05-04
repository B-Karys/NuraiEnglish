package com.example.nuraienglish.core.data.model

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    KAZAKH("kk", "Қазақша")
}

fun String.toAppLanguage(): AppLanguage =
    AppLanguage.entries.firstOrNull { it.code == this } ?: AppLanguage.ENGLISH
