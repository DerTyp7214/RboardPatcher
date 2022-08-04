package de.dertyp7214.rboardpatcher.patcher.types

import com.google.gson.annotations.SerializedName

data class ThemeMetadata(
    @SerializedName("format_version")
    val formatVersion: Int,
    var id: String,
    var name: String,
    @SerializedName("prefer_key_border")
    var preferKeyBorder: Boolean,
    @SerializedName("lock_key_border")
    var lockKeyBorder: Boolean,
    @SerializedName("is_light_theme")
    var isLightTheme: Boolean,
    @SerializedName("style_sheets")
    val styleSheets: ArrayList<String>,
    val flavors: List<Flavor>
)

data class Flavor(
    val type: String,
    @SerializedName("style_sheets")
    val styleSheets: ArrayList<String>
)