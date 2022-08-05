package de.dertyp7214.rboardpatcher.adapter.types

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorFilter
import de.dertyp7214.rboardpatcher.patcher.Theme
import java.io.File
import java.util.*

data class ThemeDataClass(
    val image: Bitmap? = null,
    val name: String,
    val path: String,
    val colorFilter: ColorFilter? = null,
    var packName: String = "",
    var isInstalled: Boolean = false
) {
    val readableName = name.split("_").joinToString(" ") { s ->
        s.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }
    }.removeSuffix(":")
    fun getTheme(context: Context) = Theme(context, File(path))
}