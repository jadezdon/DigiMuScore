package com.zhouppei.digimuscore.di

import android.content.Context
import com.zhouppei.digimuscore.data.AppDatabase
import com.zhouppei.digimuscore.data.dao.FolderDao
import com.zhouppei.digimuscore.data.dao.SheetMusicDao
import com.zhouppei.digimuscore.data.dao.SheetMusicPageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideFolderDao(appDatabase: AppDatabase): FolderDao {
        return appDatabase.folderDao()
    }

    @Provides
    fun provideSheetMusicDao(appDatabase: AppDatabase): SheetMusicDao {
        return appDatabase.sheetMusicDao()
    }

    @Provides
    fun provideSheetMusicPageDao(appDatabase: AppDatabase): SheetMusicPageDao {
        return appDatabase.sheetMusicPageDao()
    }
}