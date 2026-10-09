package com.example.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageDownloader {

    suspend fun downloadImage(context: Context, imageUrl: String, okHttpClient: OkHttpClient): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "FoxyChat_${System.currentTimeMillis()}.jpg"
                val request = Request.Builder().url(imageUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful && response.body != null) {
                    val inputStream: InputStream = response.body!!.byteStream()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FoxyChat")
                        }
                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        if (uri != null) {
                            resolver.openOutputStream(uri)?.use { out ->
                                inputStream.copyTo(out)
                            }
                            return@withContext true
                        }
                    } else {
                        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val folder = File(picturesDir, "FoxyChat").apply { mkdirs() }
                        val file = File(folder, fileName)
                        FileOutputStream(file).use { out ->
                            inputStream.copyTo(out)
                        }
                        return@withContext true
                    }
                }

                // Fallback to DownloadManager
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                if (dm != null) {
                    val dmRequest = DownloadManager.Request(Uri.parse(imageUrl))
                        .setTitle("FoxyChat_${System.currentTimeMillis()}.jpg")
                        .setDescription("Downloading photo from Foxy Chat")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FoxyChat_${System.currentTimeMillis()}.jpg")
                    dm.enqueue(dmRequest)
                    return@withContext true
                }

                false
            } catch (e: Exception) {
                // Secondary fallback
                try {
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    if (dm != null) {
                        val dmRequest = DownloadManager.Request(Uri.parse(imageUrl))
                            .setTitle("FoxyChat_${System.currentTimeMillis()}.jpg")
                            .setDescription("Downloading photo from Foxy Chat")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FoxyChat_${System.currentTimeMillis()}.jpg")
                        dm.enqueue(dmRequest)
                        return@withContext true
                    }
                } catch (_: Exception) {}
                false
            }
        }
    }
}
