package com.example.nuraienglish.core.data.local.dao

import androidx.room.*
import com.example.nuraienglish.core.data.local.entity.CachedProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress")
    fun observeAll(): Flow<List<CachedProgressEntity>>

    @Query("SELECT * FROM progress WHERE courseId = :courseId")
    suspend fun getByCourse(courseId: String): CachedProgressEntity?

    @Upsert
    suspend fun upsert(progress: CachedProgressEntity)

    @Query("DELETE FROM progress")
    suspend fun deleteAll()
}
