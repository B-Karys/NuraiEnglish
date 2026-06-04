package com.example.nuraienglish.core.data.model

fun Course.canBeOpenedBy(totalPoints: Int, isAdmin: Boolean): Boolean {
    return isAdmin || totalPoints >= requiredPointsToOpen()
}

fun Course.pointsNeeded(totalPoints: Int): Int {
    return (requiredPointsToOpen() - totalPoints).coerceAtLeast(0)
}

fun Course.requiredPointsToOpen(): Int {
    val levelPoints = minPointsForLevel(level) ?: 0
    return maxOf(pointsToUnlock, levelPoints)
}
