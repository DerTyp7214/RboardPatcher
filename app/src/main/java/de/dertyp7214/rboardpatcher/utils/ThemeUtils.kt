package de.dertyp7214.rboardpatcher.utils

import android.app.Activity
import android.content.Intent
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import de.dertyp7214.rboardpatcher.R
import java.io.File

object ThemeUtils {
    fun shareTheme(activity: Activity, themePack: File, install: Boolean = true) {
        val uri = FileProvider.getUriForFile(
            activity,
            activity.packageName,
            themePack
        )
        ShareCompat.IntentBuilder(activity)
            .setStream(uri)
            .setType("application/pack")
            .intent
            .setAction(if (install) Intent.ACTION_VIEW else Intent.ACTION_SEND)
            .setDataAndType(uri, "application/pack")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
                activity.startActivity(
                    Intent.createChooser(
                        this,
                        activity.getString(R.string.share_theme)
                    )
                )
            }
    }
}