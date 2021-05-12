package com.zhouppei.digimuscore.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zhouppei.digimuscore.data.converters.DrawLayerConverter
import com.zhouppei.digimuscore.data.dao.FolderDao
import com.zhouppei.digimuscore.data.dao.SheetMusicDao
import com.zhouppei.digimuscore.data.dao.SheetMusicPageDao
import com.zhouppei.digimuscore.data.models.Folder
import com.zhouppei.digimuscore.data.models.SheetMusic
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.workers.SeedDatabaseWorker

@Database(
    entities = [Folder::class, SheetMusic::class, SheetMusicPage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DrawLayerConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun folderDao(): FolderDao
    abstract fun sheetMusicDao(): SheetMusicDao
    abstract fun sheetMusicPageDao(): SheetMusicPageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                            WorkManager.getInstance(context).enqueue(request)
                        }
                    }
                )
                .build()
        }
    }
}