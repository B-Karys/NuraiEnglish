package com.example.nuraienglish.core.di

import android.content.Context
import androidx.room.Room
import com.example.nuraienglish.core.data.local.AppDatabase
import com.example.nuraienglish.core.data.local.dao.CourseDao
import com.example.nuraienglish.core.data.local.dao.LessonDao
import com.example.nuraienglish.core.data.local.dao.ProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wordly.db").build()

    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()

    @Provides
    fun provideLessonDao(db: AppDatabase): LessonDao = db.lessonDao()

    @Provides
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
}
