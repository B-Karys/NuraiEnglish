package com.example.nuraienglish.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nuraienglish.core.data.model.Progress

@Entity(tableName = "progress")
data class CachedProgressEntity(
    @PrimaryKey val courseId: String,
    val completedLessonsJson: String,
    val totalLessons: Int,
    val points: Int,
    val lastUpdated: Long
)

fun CachedProgressEntity.toDomain() = Progress(
    courseId = courseId,
    completedLessons = if (completedLessonsJson.isBlank()) emptyList()
                       else completedLessonsJson.split(","),
    totalLessons = totalLessons,
    points = points,
    lastUpdated = lastUpdated
)

fun Progress.toEntity() = CachedProgressEntity(
    courseId = courseId,
    completedLessonsJson = completedLessons.joinToString(","),
    totalLessons = totalLessons,
    points = points,
    lastUpdated = lastUpdated
)
