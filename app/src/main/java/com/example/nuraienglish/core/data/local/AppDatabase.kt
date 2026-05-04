package com.example.nuraienglish.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nuraienglish.core.data.local.dao.CourseDao
import com.example.nuraienglish.core.data.local.dao.LessonDao
import com.example.nuraienglish.core.data.local.dao.ProgressDao
import com.example.nuraienglish.core.data.local.entity.CachedCourseEntity
import com.example.nuraienglish.core.data.local.entity.CachedLessonEntity
import com.example.nuraienglish.core.data.local.entity.CachedProgressEntity

@Database(
    entities = [
        CachedCourseEntity::class,
        CachedLessonEntity::class,
        CachedProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun progressDao(): ProgressDao
}
