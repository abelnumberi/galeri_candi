package com.abelnumberi.galericandi.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun copyUriToInternalStorage(context: Context, uri: Uri, id: String): Uri? {
        return try {
            val contentResolver = context.contentResolver
            val cacheDir = File(context.cacheDir, "offline_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val fileName = "temple_${id}_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
