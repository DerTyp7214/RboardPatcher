package de.dertyp7214.rboardpatcher.core

import android.content.Context

fun Int.dp(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}