@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.dertyp7214.rboardpatcher.patcher

import android.content.Context
import com.google.gson.GsonBuilder
import de.dertyp7214.rboardpatcher.core.newId
import de.dertyp7214.rboardpatcher.patcher.types.ThemeMetadata
import de.dertyp7214.rboardpatcher.utils.ZipHelper
import de.dertyp7214.rboardpatcher.utils.doAsync
import java.io.File

class Patcher(private val context: Context) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val patcherPath = File(context.cacheDir, "patcher")
    private val patchedThemesPath = File(context.cacheDir, "patchedThemes")

    init {
        if (!patcherPath.exists()) patcherPath.mkdirs()
        if (!patchedThemesPath.exists()) patchedThemesPath.mkdirs()
    }

    fun clearCache() {
        patcherPath.listFiles()?.forEach { it.deleteRecursively() }
        patchedThemesPath.listFiles()?.forEach { it.deleteRecursively() }
    }

    fun patchTheme(
        theme: Theme,
        vararg patches: Patch,
        clean: Boolean = true,
        progress: (Float, String) -> Unit = { _, _ -> }
    ): Pair<File, File?> {
        val patchFiles = arrayListOf<File>()
        patches.forEach { patch ->
            progress((patches.indexOf(patch) + 1f) / patches.size * 100f, patch.patchMeta.name)
            patch.getPatches(context, patcherPath)
                .listFiles { file -> !file.name.endsWith(".meta") }
                ?.forEach(patchFiles::add)
        }
        val borderCssFiles = arrayListOf<File>()
        val cssFiles = patchFiles.filter { it.name.endsWith(".css") }.filterNot {
            it.name.endsWith("_border.css").also { border -> if (border) borderCssFiles.add(it) }
        }

        val patchingPath = File(context.cacheDir, "patching")
        val patchedPath = File(patchedThemesPath, "${theme.name}.zip")
        if (!patchingPath.exists()) patchingPath.mkdirs()
        if (ZipHelper().unpackZip(patchingPath.absolutePath, theme.themePath.absolutePath)) {
            val metadata = gson.fromJson(
                File(patchingPath, "metadata.json").readText(),
                ThemeMetadata::class.java
            )
            metadata.newId()
            patchFiles.forEach {
                it.copyRecursively(File(patchingPath, it.name), true)
            }
            cssFiles.forEach {
                metadata.styleSheets.add(it.name)
            }
            borderCssFiles.forEach {
                metadata.flavors.find { it.type.lowercase() == "border" }?.styleSheets?.add(it.name)
            }
            File(patchingPath, "metadata.json").writeText(gson.toJson(metadata))
            patchingPath.listFiles()?.let {
                ZipHelper().zip(
                    it.map { file -> file.absolutePath },
                    patchedPath.absolutePath
                )
            }
            theme.imagePath.let {
                if (it.exists()) it.copyTo(File(patchedThemesPath, theme.name), true)
            }
        }

        patchingPath.deleteRecursively()

        if (clean) {
            theme.delete()
            patches.forEach { it.clean(context, patcherPath) }
        }

        return Pair(patchedPath, File(patchedThemesPath, theme.name).let {
            if (it.exists()) it
            else null
        })
    }

    fun patchTheme(
        theme: Theme,
        vararg patches: Patch,
        clean: Boolean = true,
        progress: (Float, String) -> Unit = { _, _ -> },
        callback: (Pair<File, File?>) -> Unit
    ) {
        doAsync({ patchTheme(theme, *patches, clean = clean, progress = progress) }, callback)
    }
}