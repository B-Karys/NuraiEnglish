package com.example.nuraienglish.core.data.model

data class LearningLevel(
    val label: String,
    val minPoints: Int,
    val maxPoints: Int
)

val learningLevels = listOf(
    LearningLevel("A1", 0, 30),
    LearningLevel("A2", 30, 100),
    LearningLevel("B1", 100, 160),
    LearningLevel("B2", 170, 210)
)

fun learningLevelForPoints(points: Int): LearningLevel {
    return learningLevels.lastOrNull { points >= it.minPoints } ?: learningLevels.first()
}

fun levelProgressFraction(points: Int): Float {
    val maxPoints = learningLevels.last().maxPoints
    return points.coerceIn(0, maxPoints).toFloat() / maxPoints
}

fun minPointsForLevel(level: String): Int? {
    val normalized = level.trim()
        .uppercase()
        .replace('\u0410', 'A')
        .replace('\u0412', 'B')
        .replace('\u0411', 'B')

    return learningLevels.firstOrNull { it.label == normalized }?.minPoints
}
