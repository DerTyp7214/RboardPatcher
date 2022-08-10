@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.dertyp7214.rboardpatcher.patcher

import android.content.Context
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.core.downloadFile
import de.dertyp7214.rboardpatcher.utils.ZipHelper
import de.dertyp7214.rboardpatcher.utils.doAsync
import java.io.File

class Patch(val patchMeta: PatchMeta) {
    fun getPatches(context: Context, patcherPath: File = context.cacheDir): File {
        val path = File(patcherPath, patchMeta.name)
        val zip = File(patcherPath, "${patchMeta.name}.zip")
        patchMeta.url.downloadFile(zip)
        ZipHelper().unpackZip(path.absolutePath, zip.absolutePath)
        zip.delete()
        path.listFiles()?.forEach {
            if (!it.name.endsWith(".png")) {
                it.copyTo(
                    File(
                        path,
                        "${patchMeta.getSafeName()}_${it.name}"
                    ), true
                )
                it.delete()
            }
        }
        return path
    }

    fun getPatches(
        context: Context,
        patcherPath: File = context.cacheDir,
        callback: (File) -> Unit
    ) {
        doAsync({ getPatches(context, patcherPath) }, callback)
    }

    fun clean(context: Context, patcherPath: File = context.cacheDir) {
        File(patcherPath, patchMeta.name).deleteRecursively()
    }
}