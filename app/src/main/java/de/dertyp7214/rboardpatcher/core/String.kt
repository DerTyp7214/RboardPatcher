package de.dertyp7214.rboardpatcher.core

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun String.downloadFile(path: File): File {
    if (path.exists()) path.deleteRecursively()
    URL(this).openStream().use { Files.copy(it, path.toPath(), StandardCopyOption.REPLACE_EXISTING) }
    return path
}