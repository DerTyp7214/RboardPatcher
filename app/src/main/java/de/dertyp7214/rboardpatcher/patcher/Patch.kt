@file:Suppress("MemberVisibilityCanBePrivate")

package de.dertyp7214.rboardpatcher.patcher

import android.content.Context
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.utils.doAsync
import java.io.File
import java.net.URL
import java.nio.file.Files

class Patch(private val patchMeta: PatchMeta) {
    fun getPatches(context: Context): File {
        val path = File(context.cacheDir, "${patchMeta.name}.zip")
        URL(patchMeta.url).openStream().use { Files.copy(it, path.toPath()) }
        return path
    }

    fun getPatches(context: Context, callback: (File) -> Unit) {
        doAsync({ getPatches(context) }, callback)
    }
}