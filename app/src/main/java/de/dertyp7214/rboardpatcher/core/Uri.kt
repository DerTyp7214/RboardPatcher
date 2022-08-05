package de.dertyp7214.rboardpatcher.core

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

fun Uri.writeToFile(context: Context, file: File): Boolean {
    if (!file.exists()) file.createNewFile()
    return context.contentResolver.openInputStream(this)?.let {
        file.copyInputStreamToFile(it)
        it.close()
        true
    } ?: false
}

fun Uri.path(context: Context): String? {
    val path: String?
    val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
    val cursor = context.contentResolver.query(this, projection, null, null, null)

    if (cursor == null) path = getPath()
    else {
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndexOrThrow(projection.first())
        path = cursor.getString(columnIndex)
        cursor.close()
    }

    return if (path.isNullOrEmpty()) getPath() else path
}