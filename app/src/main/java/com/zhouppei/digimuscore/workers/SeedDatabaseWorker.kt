package com.zhouppei.digimuscore.workers

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.zhouppei.digimuscore.data.AppDatabase
import com.zhouppei.digimuscore.data.models.Folder
import com.zhouppei.digimuscore.data.models.SheetMusic
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import com.zhouppei.digimuscore.notation.DrawLayer
import com.zhouppei.digimuscore.utils.BitmapUtil
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.utils.FileUtil
import kotlinx.coroutines.coroutineScope
import java.io.FileOutputStream
import java.io.InputStream

class SeedDatabaseWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = AppDatabase.getInstance(applicationContext)

            applicationContext.assets.open(Constants.SHEET_MUSIC_DATA_FILENAME).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val sheetMusicType = object : TypeToken<List<SheetMusic>>() {}.type
                    val sheetMusicList: List<SheetMusic> =
                        Gson().fromJson(jsonReader, sheetMusicType)

                    database.folderDao().insert(Folder(0, "Default"))
                    database.sheetMusicDao().insertAll(*sheetMusicList.toTypedArray())
                }
            }
            applicationContext.assets.open(Constants.SHEET_MUSIC_KISS_THE_RAIN_FILENAME).use { inputStream ->
                inputStream.use { input ->
                    val pages = getSheetMusicPagesFromInputStream(input)
                    database.sheetMusicPageDao().insertAll(*pages.toTypedArray())
                }
            }
            Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }

    private fun getSheetMusicPagesFromInputStream(input: InputStream): ArrayList<SheetMusicPage> {
        val tempFile = FileUtil.createOrGetFile(
            context,
            "Sheetmusics",
            "${FileUtil.getFileNameNoExt()}.png"
        )
        FileOutputStream(tempFile).use { output ->
            val buffer = ByteArray(4 * 1024) // buffer size
            while (true) {
                val byteCount = input.read(buffer)
                if (byteCount < 0) break
                output.write(buffer, 0, byteCount)
            }
            output.flush()
        }
        val bitmaps = BitmapUtil.pdfToBitmap(tempFile)
        tempFile.delete()
        val contentUris = arrayListOf<String>()
        val imageSizes = arrayListOf<Size>()
        val pages = arrayListOf<SheetMusicPage>()
        bitmaps?.forEach { bitmap ->
            val destinationFile = FileUtil.createOrGetFile(
                context,
                "Sheetmusics",
                "${FileUtil.getFileNameNoExt()}.png"
            )

            BitmapUtil.saveBitmapToFile(bitmap, destinationFile.path.toString())
            contentUris.add(destinationFile.path.toString())
            imageSizes.add(Size(bitmap.width, bitmap.height))
        }
        contentUris.forEachIndexed { index, contentUri ->
            val defaultLayer = DrawLayer("Default", 0).apply {
                pageWidth = imageSizes[index].width
                pageHeight = imageSizes[index].height
            }
            val page = SheetMusicPage(
                sheetMusicId = 1,
                contentUri = contentUri,
                pageNumber = index + 1
            )
            page.drawLayers.add(defaultLayer)
            pages.add(page)
        }
        return pages
    }

    companion object {
        private val TAG = SeedDatabaseWorker::class.qualifiedName
    }
}