package com.example.nuraienglish.core.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val language: String = AppLanguage.ENGLISH.code,
    val points: Int = 0,
    val currentLevel: String = "A1",
    val unlockedLevels: List<String> = listOf("A1"),
    val onboardingComplete: Boolean = false,
    val isAdmin: Boolean = false
)
