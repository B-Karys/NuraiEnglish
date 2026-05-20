package com.example.nuraienglish.core.data.local.dao

import androidx.room.*
import com.example.nuraienglish.core.data.local.entity.CachedCourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY `order` ASC")
    fun observeAll(): Flow<List<CachedCourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getById(id: String): CachedCourseEntity?

    @Upsert
    suspend fun upsertAll(courses: List<CachedCourseEntity>)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
