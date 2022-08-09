package de.dertyp7214.rboardpatcher.components

import androidx.appcompat.app.AppCompatActivity
import de.dertyp7214.rboardpatcher.utils.isPackageInstalled

open class BaseActivity: AppCompatActivity() {
    val managerPackage = "de.dertyp7214.rboardthememanager"
    val managerPackageName by lazy {
        if (isPackageInstalled(
                managerPackage,
                packageManager
            )
        ) managerPackage
        else if (isPackageInstalled(
                "$managerPackage.debug",
                packageManager
            )
        ) "$managerPackage.debug"
        else null
    }
    val managerInstalled by lazy { managerPackageName != null }
}