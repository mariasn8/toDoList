package com.example.todolist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object FileHelper {
    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        // 1. Get the file name from the URI
        var fileName = "attachment_${System.currentTimeMillis()}"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) fileName = it.getString(nameIndex)
            }
        }

        // 2. Create a file in the app's internal "files" directory
        val file = File(context.filesDir, fileName)

        // 3. Copy the content
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath // Return the path to save in DB
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openFile(context: Context, path: String) {
        val file = File(path)
        if (file.exists()) {
            // Get the URI using the FileProvider we set up
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Must match AndroidManifest authorities
                file
            )

            // Determine the file type (MIME type)
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"

            // Create the intent to view the file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch the external app
            try {
                context.startActivity(intent)
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }
}