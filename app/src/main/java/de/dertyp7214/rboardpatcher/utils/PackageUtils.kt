package de.dertyp7214.rboardpatcher.utils

import android.content.pm.PackageManager
import android.os.Build

@Suppress("DEPRECATION")
fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= 33)
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
        else packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}