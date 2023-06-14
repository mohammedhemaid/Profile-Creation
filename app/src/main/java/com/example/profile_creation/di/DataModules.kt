package com.example.profile_creation.di

import android.content.Context
import androidx.room.Room
import com.example.profile_creation.data.AppDatabase
import com.example.profile_creation.data.UserDao
import com.example.profile_creation.repository.UserRepository
import com.example.profile_creation.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindNoteRepository(repository: UserRepositoryImpl): UserRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database.db"
        ).build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): UserDao = database.userDao()
}