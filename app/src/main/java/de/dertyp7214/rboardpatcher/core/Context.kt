package de.dertyp7214.rboardpatcher.core

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import java.io.File

fun Context.clearTmp() {
    File(cacheDir, "tmp").listFiles()?.forEach { it.deleteRecursively() }
}

fun Context.getAttr(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}