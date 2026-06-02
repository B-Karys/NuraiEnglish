package com.example.nuraienglish.core.data.model

fun Course.canBeOpenedBy(totalPoints: Int, isAdmin: Boolean): Boolean {
    return isAdmin || totalPoints >= pointsToUnlock
}

fun Course.pointsNeeded(totalPoints: Int): Int {
    return (pointsToUnlock - totalPoints).coerceAtLeast(0)
}
