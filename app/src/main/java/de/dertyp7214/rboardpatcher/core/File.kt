package de.dertyp7214.rboardpatcher.core

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import de.dertyp7214.rboardpatcher.BuildConfig
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}

@SuppressLint("SdCardPath")
fun File.decodeBitmap(opts: BitmapFactory.Options? = null): Bitmap? {
    val pathName = absolutePath
    var bm: Bitmap? = null
    var stream: InputStream? = null
    try {
        stream = FileInputStream(pathName)
        bm = BitmapFactory.decodeStream(stream, null, opts)
    } catch (e: Exception) {
        Log.e("BitmapFactory", "Unable to decode stream: $e")
        try {
            val file = File("/data/data/${BuildConfig.APPLICATION_ID}")
            this.copyTo(file)
            stream = FileInputStream(file)
            bm = BitmapFactory.decodeStream(stream, null, opts)
        } catch (e: Exception) {
            Log.e("BitmapFactory", "Unable to decode stream: $e")
        }
    } finally {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                // do nothing here
            }
        }
    }
    return bm
}