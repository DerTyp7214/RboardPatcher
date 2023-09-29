package de.dertyp7214.rboardpatcher.utils

import android.content.pm.PackageManager
import android.os.Build

@Suppress("DEPRECATION")
fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= 33)
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()

        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}