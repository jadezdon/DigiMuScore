package com.zhouppei.digimuscore.utils
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    @JvmStatic
    fun copyFile(uri: Uri, context: Context, fileNameNoExt: String): Uri {
        val destinationFile = createOrGetFile(
            context,
            "$fileNameNoExt.png"
        )

        try {
            val fos = FileOutputStream(destinationFile)
            val inputStream = context.contentResolver.openInputStream(uri)
            val buffer = ByteArray(1024)

            if (inputStream != null) {
                var readBytes: Int
                while (inputStream.read(buffer).let { readBytes = it; it != -1 }) {
                    fos.write(buffer, 0, readBytes)
                }

                inputStream.close()
            }

            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.fromFile(destinationFile)
    }

    @JvmStatic
    fun createOrGetFile(context: Context, fileName: String): File {
        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!, Constants.SHEET_MUSIC_FOLDER_NAME)

        if (!folder.exists())
            folder.mkdir()

        val destFile = File(folder, fileName)

        destFile.createNewFile()

        return destFile
    }

    @JvmStatic
    fun getFileNameNoExt(sheetMusicId: Int): String {
        val simpleDateFormat = SimpleDateFormat("yyMMddHHmmssSSS", Locale.getDefault())
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        return "sheetmusic${sheetMusicId}-${currentDateAndTime}"
    }

    @JvmStatic
    fun deleteAllRelatedFiles(uri: String) {
        val folderName = uri.substringBeforeLast("/") + "/"
        val fileNamePrefixNoExt = uri.substringAfterLast("/").substringBeforeLast("-")
        File(folderName).walk().forEach { file ->
            if (file.name.contains(fileNamePrefixNoExt))
                file.delete()
        }
    }

    @JvmStatic
    fun deleteFile(uri: String) {
        val folderName = uri.substringBeforeLast("/") + "/"
        val fileNameNoExt = uri.substringAfterLast("/").substringBeforeLast(".")
        File(folderName).walk().forEach { file ->
            if (file.name.contains(fileNameNoExt))
                file.delete()
        }
    }
}
