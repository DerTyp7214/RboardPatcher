@file:Suppress("MemberVisibilityCanBePrivate")

package de.dertyp7214.rboardpatcher

import de.dertyp7214.colorutilsc.ColorUtilsC
import de.dertyp7214.rboardcomponents.utils.ThemeUtils
import de.dertyp7214.rboardpatcher.core.clearTmp
import de.dertyp7214.rboardpatcher.patcher.Patcher
import de.dertyp7214.rboardpatcher.patcher.ThemePack
import de.dertyp7214.rboardpatcher.screens.MainActivity

class Application : android.app.Application() {
    val patcher by lazy { Patcher(this) }

    override fun onCreate() {
        super.onCreate()
        ThemeUtils.registerActivityLifecycleCallbacks(this)
        ThemeUtils.applyTheme(this) { activity, changed ->
            if (activity is MainActivity && changed) activity.recreate()
        }

        ColorUtilsC.init()

        clearTmp()
        ThemePack.clearCache(this)
        patcher.clearCache()
    }
}