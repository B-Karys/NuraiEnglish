package com.example.nuraienglish.core.data.local.dao

import androidx.room.*
import com.example.nuraienglish.core.data.local.entity.CachedLessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY `order` ASC")
    fun observeByCourse(courseId: String): Flow<List<CachedLessonEntity>>

    @Upsert
    suspend fun upsertAll(lessons: List<CachedLessonEntity>)

    @Query("DELETE FROM lessons WHERE courseId = :courseId")
    suspend fun deleteByCourse(courseId: String)
}
