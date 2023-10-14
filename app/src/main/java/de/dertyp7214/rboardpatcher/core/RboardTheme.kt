package de.dertyp7214.rboardpatcher.core

import android.content.Context
import android.graphics.Bitmap
import de.dertyp7214.rboard.RboardTheme
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun RboardTheme.toZip(context: Context): File {
    val dir = File(context.cacheDir, "import")
    val path = File(dir, "${name}.zip")

    val buffer = 80000

    if (!dir.exists()) dir.mkdirs()
    if (path.exists()) path.deleteRecursively()

    ZipOutputStream(
        BufferedOutputStream(
            path.outputStream()
        )
    ).use { out ->
        val data = ByteArray(buffer)

        val metadata = ZipEntry("metadata.json")
        out.putNextEntry(metadata)
        out.write(this.metadata.toString().toByteArray())

        for (i in css.indices) {
            val entry = ZipEntry(css[i].name)
            out.putNextEntry(entry)
            out.write(css[i].content.toByteArray())
        }

        for (i in images.indices) {
            val entry = ZipEntry(images[i].name)
            out.putNextEntry(entry)
            out.write(images[i].content.toByteArray())
        }

    }

    if (preview != null) {
        val outputStream = FileOutputStream(File(dir, name))
        preview!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    return path
}