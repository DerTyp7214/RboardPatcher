@file:Suppress("MemberVisibilityCanBePrivate")

package de.dertyp7214.rboardpatcher

import com.google.android.material.color.DynamicColors
import de.dertyp7214.colorutilsc.ColorUtilsC
import de.dertyp7214.rboardpatcher.core.clearTmp
import de.dertyp7214.rboardpatcher.patcher.Patcher
import de.dertyp7214.rboardpatcher.patcher.ThemePack

class Application : android.app.Application() {
    val patcher by lazy { Patcher(this) }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        ColorUtilsC.init()

        clearTmp()
        ThemePack.clearCache(this)
        patcher.clearCache()
    }
}