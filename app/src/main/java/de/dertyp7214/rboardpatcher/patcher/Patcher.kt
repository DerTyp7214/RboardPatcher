@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.dertyp7214.rboardpatcher.patcher

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import de.dertyp7214.rboardpatcher.core.newId
import de.dertyp7214.rboardpatcher.patcher.types.FileMap
import de.dertyp7214.rboard.ThemeMetadata
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

    fun preparePatch(theme: Theme): List<File>? {
        val patchingPath = File(context.cacheDir, "patching")
        patchingPath.deleteRecursively()
        if (!patchingPath.exists()) patchingPath.mkdirs()
        ZipHelper().unpackZip(patchingPath.absolutePath, theme.themePath.absolutePath)
        return patchingPath.listFiles()?.toList()
    }

    fun patchTheme(
        theme: Theme,
        toRemove: HashMap<String, List<String>>,
        vararg patches: Patch,
        clean: Boolean = true,
        progress: (Float, String) -> Unit = { _, _ -> }
    ): Pair<File, File?> {
        val patchFiles = arrayListOf<File>()
        val fileMap = FileMap(arrayListOf(), hashMapOf())
        val customValuesCss = StringBuilder()
        patches.forEach { patch ->
            val customValue = patch.patchMeta.customValue
            progress((patches.indexOf(patch) + 1f) / patches.size * 100f, patch.patchMeta.name)

            if (customValue != null) customValuesCss.appendLine("@def ${customValue.first} ${customValue.second};")

            patch.getPatches(context, patcherPath)
                .listFiles { file -> !file.name.endsWith(".meta") }?.apply {
                    val customImage = patch.patchMeta.customImage
                    forEach(patchFiles::add)
                    fileMap.patches.add(patch.patchMeta.getSafeName())
                    fileMap.patchFiles[patch.patchMeta.getSafeName()] = this.map {
                        if (it.name == customImage?.first)
                            customImage?.second?.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                it.outputStream()
                            )
                        it.name
                    }
                }
        }
        if (customValuesCss.isNotEmpty()) {
            val customValuesFile = File(patcherPath, "custom_values.css")
            customValuesFile.writeText(customValuesCss.toString())
            patchFiles.add(customValuesFile)
        }
        val borderCssFiles = arrayListOf<File>()
        val cssFiles = patchFiles.filter { it.name.endsWith(".css") }.filterNot {
            it.name.endsWith("_border.css").also { border -> if (border) borderCssFiles.add(it) }
        }

        val patchingPath = File(context.cacheDir, "patching")
        val patchedPath = File(patchedThemesPath, "${theme.name}.zip")
        if (!patchingPath.exists()) patchingPath.mkdirs()

        val filesToRemove = arrayListOf<String>().apply {
            toRemove.forEach { (_, strings) -> addAll(strings) }
        }

        val metadata = gson.fromJson(
            File(patchingPath, "metadata.json").readText(),
            ThemeMetadata::class.java
        )
        metadata.newId()
        patchFiles.forEach {
            it.copyRecursively(File(patchingPath, it.name), true)
        }

        if (filesToRemove.isNotEmpty()) {
            metadata.styleSheets.apply {
                val tmp = filter { file -> !filesToRemove.contains(file) }
                clear()
                addAll(tmp)
            }
            metadata.flavors.find { it.type.lowercase() == "border" }?.styleSheets?.apply {
                val tmp = this.filter { file -> !filesToRemove.contains(file) }
                clear()
                addAll(tmp)
            }
        }

        cssFiles.forEach {
            metadata.styleSheets.apply {
                if (!contains(it.name)) add(it.name)
            }
        }
        borderCssFiles.forEach { file ->
            metadata.flavors.find { it.type.lowercase() == "border" }?.styleSheets?.apply {
                if (!contains(file.name)) add(file.name)
            }
        }
        File(patchingPath, "metadata.json").writeText(gson.toJson(metadata))
        File(patchingPath, "fileMap.json").writeText(gson.toJson(fileMap))
        patchingPath.listFiles()?.let {
            ZipHelper().zip(
                it.filter { file -> !filesToRemove.contains(file.name) }
                    .map { file -> file.absolutePath },
                patchedPath.absolutePath
            )
        }
        theme.imagePath.let {
            if (it.exists()) it.copyTo(File(patchedThemesPath, theme.name), true)
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
        toRemove: HashMap<String, List<String>>,
        vararg patches: Patch,
        clean: Boolean = true,
        progress: (Float, String) -> Unit = { _, _ -> },
        callback: (Pair<File, File?>) -> Unit
    ) {
        doAsync(
            { patchTheme(theme, toRemove, *patches, clean = clean, progress = progress) },
            callback
        )
    }
}