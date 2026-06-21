package com.tiswu.sagex.di

import android.content.Context
import androidx.room.Room
import com.tiswu.sagex.data.local.MessageDao
import com.tiswu.sagex.data.local.SageXDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): SageXDatabase =
        Room.databaseBuilder(context, SageXDatabase::class.java, SageXDatabase.NAME)
            .addMigrations(SageXDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideMessageDao(database: SageXDatabase): MessageDao = database.messageDao()
}
