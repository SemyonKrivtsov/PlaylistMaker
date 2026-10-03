package com.example.playlistmaker.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class ImageStorage(private val context: Context) {

    suspend fun saveImageToPrivateStorage(uriString: String): String? =
        withContext(Dispatchers.IO) {
            val directory = File(context.filesDir, COVERS_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "${UUID.randomUUID()}.jpg")

            try {
                val isSaved = context.contentResolver
                    .openInputStream(uriString.toUri())
                    ?.use { input ->
                        val bitmap = BitmapFactory.decodeStream(input)
                        bitmap?.let {
                            FileOutputStream(file).use { output ->
                                it.compress(Bitmap.CompressFormat.JPEG, COVER_QUALITY, output)
                            }
                        } ?: false
                    } ?: false

                if (isSaved) {
                    file.absolutePath
                } else {
                    file.delete()
                    null
                }
            } catch (e: IOException) {
                file.delete()
                null
            } catch (e: SecurityException) {
                file.delete()
                null
            }
        }

    companion object {
        private const val COVERS_DIRECTORY = "playlist_covers"
        private const val COVER_QUALITY = 30
    }
}
