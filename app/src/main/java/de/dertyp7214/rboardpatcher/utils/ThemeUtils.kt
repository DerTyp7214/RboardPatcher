package de.dertyp7214.rboardpatcher.utils

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import de.dertyp7214.rboardpatcher.R
import java.io.File

object ThemeUtils {
    fun shareTheme(activity: Activity, themePack: File, install: Boolean = true, resultLauncher: ActivityResultLauncher<Intent>? = null) {
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
                if (install) {
                    if (isPackageInstalled(
                            "de.dertyp7214.rboardthememanager",
                            activity.packageManager
                        )
                    ) setPackage("de.dertyp7214.rboardthememanager")
                    else if (isPackageInstalled(
                            "de.dertyp7214.rboardthememanager.debug",
                            activity.packageManager
                        )
                    ) setPackage("de.dertyp7214.rboardthememanager.debug")
                }
                val intent = Intent.createChooser(
                    this,
                    activity.getString(R.string.share_theme)
                )

                resultLauncher?.launch(intent) ?: activity.startActivity(intent)
            }
    }
}