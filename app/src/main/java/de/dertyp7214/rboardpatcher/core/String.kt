package de.dertyp7214.rboardpatcher.core

import android.os.Build
import de.dertyp7214.rboardpatcher.adapter.types.ThemeDataClass
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun String.downloadFile(path: File): File {
    if (path.exists()) path.deleteRecursively()
    path.parentFile?.let { if (!it.exists()) it.mkdirs() }
    URL(this).openStream()
        .use { Files.copy(it, path.toPath(), StandardCopyOption.REPLACE_EXISTING) }
    return path
}

fun String.parseThemeDataClass(): ThemeDataClass {
    val file = File(this)
    val name = file.nameWithoutExtension
    val path = file.absolutePath
    val imageFile = File(file.absolutePath.removeSuffix(".zip"))
    val image = imageFile.exists().let {
        if (it) {
            imageFile.decodeBitmap()
        } else null
    }
    return ThemeDataClass(image, name, path)
}