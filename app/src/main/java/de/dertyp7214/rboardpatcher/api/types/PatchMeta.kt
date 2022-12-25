package de.dertyp7214.rboardpatcher.api.types

import android.graphics.Bitmap

typealias KeyValue<K> = Pair<String, K>

data class PatchMeta(
    val url: String,
    val author: String,
    val tags: List<String>,
    val size: Long,
    val date: Long,
    val name: String,
    val font: String?,
    val description: String?,
    val customName: String?,
) {
    fun getSafeName() = url.split("/").last().removeSuffix(".zip")

    var customValue: KeyValue<String>? = null
    var customImage: KeyValue<Bitmap>? = null
}