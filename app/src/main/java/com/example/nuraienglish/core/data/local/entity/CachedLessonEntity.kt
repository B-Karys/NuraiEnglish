package com.example.nuraienglish.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nuraienglish.core.data.model.Lesson

@Entity(tableName = "lessons")
data class CachedLessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val titleEn: String,
    val titleRu: String,
    val titleKk: String,
    val order: Int,
    val taskCount: Int,
    val pointsReward: Int,
    val isPublished: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedLessonEntity.toDomain() = Lesson(
    id = id,
    courseId = courseId,
    titleEn = titleEn,
    titleRu = titleRu,
    titleKk = titleKk,
    order = order,
    taskCount = taskCount,
    pointsReward = pointsReward,
    isPublished = isPublished
)

fun Lesson.toEntity() = CachedLessonEntity(
    id = id,
    courseId = courseId,
    titleEn = titleEn,
    titleRu = titleRu,
    titleKk = titleKk,
    order = order,
    taskCount = taskCount,
    pointsReward = pointsReward,
    isPublished = isPublished
)
