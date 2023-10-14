package de.dertyp7214.rboardpatcher.core

import android.graphics.BitmapFactory
import com.google.gson.Gson
import de.dertyp7214.rboard.CssFile
import de.dertyp7214.rboard.ImageFile
import de.dertyp7214.rboard.RboardTheme
import de.dertyp7214.rboard.ThemeMetadata
import de.dertyp7214.rboardpatcher.adapter.types.ThemeDataClass
import java.util.zip.ZipFile

fun ThemeDataClass.toRboardTheme(): RboardTheme {
    val zip = ZipFile(path)
    val metadata = Gson().fromJson(
        zip.getInputStream(zip.getEntry("metadata.json")).use { it.bufferedReader().readText() },
        ThemeMetadata::class.java
    )
    val cssFiles = metadata.styleSheets.map { Pair(it, zip.getInputStream(zip.getEntry(it))) }
        .map { CssFile(it.first, it.second.use { stream -> stream.bufferedReader().readText() }) }
        .let { cssFiles ->
            val arrayList: ArrayList<CssFile> = ArrayList(cssFiles)
            if (metadata.flavors.isNotEmpty()) {
                arrayList.addAll(metadata.flavors.flatMap { flavor ->
                    flavor.styleSheets.map { Pair(it, zip.getInputStream(zip.getEntry(it))) }
                        .map {
                            CssFile(
                                it.first,
                                it.second.use { stream -> stream.bufferedReader().readText() })
                        }
                })
            }
            arrayList
        }
    val imageFiles = zip.entries().toList().filter { it.name.endsWith(".png") }
    val images =
        if (imageFiles.isNotEmpty()) imageFiles.map { Pair(it.name, zip.getInputStream(it)) }
            .map {
                ImageFile(
                    it.first,
                    it.second.use { stream -> BitmapFactory.decodeStream(stream) })
            } else listOf()

    zip.close()

    return RboardTheme(
        name,
        metadata,
        image,
        cssFiles,
        images
    )
}