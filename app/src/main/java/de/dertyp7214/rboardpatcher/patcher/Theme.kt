@file:Suppress("unused")

package de.dertyp7214.rboardpatcher.patcher

import android.content.Context
import de.dertyp7214.rboardpatcher.core.downloadFile
import de.dertyp7214.rboardpatcher.utils.ZipHelper
import java.io.File

class ThemePack(private val context: Context, path: File) {
    private val themePacksPath = File(context.cacheDir, "themePacks")
    private val themePackPath = File(themePacksPath, path.name)

    init {
        if (!themePacksPath.exists()) themePacksPath.mkdirs()
        path.copyTo(themePackPath, true)
        path.deleteRecursively()
    }

    fun getThemes(): List<Theme> {
        val path = File(
            themePacksPath,
            themePackPath.name.removeSuffix(".pack").removeSuffix(".zip")
        )
        return if (ZipHelper().unpackZip(path.absolutePath, themePackPath.absolutePath)) {
            val list = arrayListOf<Theme>()
            path.listFiles { file -> file.name.endsWith(".zip") }?.forEach { theme ->
                list.add(Theme(context, theme))
            }
            return list
        } else listOf()
    }

    companion object {
        operator fun invoke(context: Context, url: String): ThemePack {
            val tmpPath = File(context.cacheDir, "tmp")
            if (!tmpPath.exists()) tmpPath.mkdirs()
            return ThemePack(context, url.downloadFile(File(tmpPath, url.split("/").last())))
        }

        fun clearCache(context: Context) {
            val themePacksPath = File(context.cacheDir, "themePacks")
            themePacksPath.listFiles()?.forEach { it.deleteRecursively() }
        }
    }
}

class Theme(context: Context, path: File) {
    private val themesPath = File(context.cacheDir, "themes")
    val themePath = File(themesPath, path.name)
    private val privateImagePath = File(path.absolutePath.removeSuffix(".zip"))
    val imagePath = File(themesPath, path.name.removeSuffix(".zip"))

    val name: String
        get() = themePath.name.removeSuffix(".zip")

    init {
        if (!themesPath.exists()) themesPath.mkdirs()
        path.copyTo(themePath, true)
        path.deleteRecursively()
        if (privateImagePath.exists()) {
            privateImagePath.copyTo(File(themePath.absolutePath.removeSuffix(".zip")), true)
            privateImagePath.deleteRecursively()
        }
    }

    fun delete() {
        if (themePath.exists()) themePath.deleteRecursively()
        if (imagePath.exists()) imagePath.deleteRecursively()
    }

    fun copyTo(path: File, name: String = themePath.name): Pair<File, File?> {
        val parsedName = name.removeSuffix(".zip")
        if (themePath.exists()) themePath.copyTo(File(path, "$parsedName.zip"), true)
        if (imagePath.exists()) imagePath.copyTo(File(path, parsedName), true)
        return Pair(File(path, "$parsedName.zip"), imagePath.exists().let {
            if (it) File(path, parsedName)
            else null
        })
    }

    companion object {
        operator fun invoke(context: Context, url: String): Theme {
            val tmpPath = File(context.cacheDir, "tmp")
            if (!tmpPath.exists()) tmpPath.mkdirs()
            return Theme(context, url.downloadFile(File(tmpPath, url.split("/").last())))
        }
    }
}