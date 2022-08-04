package de.dertyp7214.rboardpatcher.core

import android.content.Context
import java.io.File

fun Context.clearTmp() {
    File(cacheDir, "tmp").listFiles()?.forEach { it.deleteRecursively() }
}