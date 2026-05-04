package com.example.nuraienglish.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nuraienglish.core.data.model.Course
import com.example.nuraienglish.core.data.model.CourseType

@Entity(tableName = "courses")
data class CachedCourseEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleRu: String,
    val titleKk: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val descriptionKk: String,
    val level: String,
    val type: String,
    val order: Int,
    val lessonCount: Int,
    val pointsToUnlock: Int,
    val isPublished: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedCourseEntity.toDomain() = Course(
    id = id,
    titleEn = titleEn,
    titleRu = titleRu,
    titleKk = titleKk,
    descriptionEn = descriptionEn,
    descriptionRu = descriptionRu,
    descriptionKk = descriptionKk,
    level = level,
    type = CourseType.entries.firstOrNull { it.name == type } ?: CourseType.VOCABULARY,
    order = order,
    lessonCount = lessonCount,
    pointsToUnlock = pointsToUnlock,
    isPublished = isPublished
)

fun Course.toEntity() = CachedCourseEntity(
    id = id,
    titleEn = titleEn,
    titleRu = titleRu,
    titleKk = titleKk,
    descriptionEn = descriptionEn,
    descriptionRu = descriptionRu,
    descriptionKk = descriptionKk,
    level = level,
    type = type.name,
    order = order,
    lessonCount = lessonCount,
    pointsToUnlock = pointsToUnlock,
    isPublished = isPublished
)
