@file:Suppress("MemberVisibilityCanBePrivate")

package de.dertyp7214.rboardpatcher

import android.util.Log
import de.dertyp7214.colorutilsc.ColorUtilsC
import de.dertyp7214.rboard.IRboard
import de.dertyp7214.rboardcomponents.utils.RboardUtils
import de.dertyp7214.rboardcomponents.utils.ThemeUtils
import de.dertyp7214.rboardpatcher.core.clearTmp
import de.dertyp7214.rboardpatcher.patcher.Patcher
import de.dertyp7214.rboardpatcher.patcher.ThemePack
import de.dertyp7214.rboardpatcher.screens.MainActivity

class Application : android.app.Application() {
    val patcher by lazy { Patcher(this) }

    companion object {
        var rboardService: IRboard? = null
    }

    override fun onCreate() {
        super.onCreate()
        ThemeUtils.registerActivityLifecycleCallbacks(this)
        ThemeUtils.applyTheme(this) { activity, changed ->
            if (activity is MainActivity && changed) activity.recreate()
        }

        RboardUtils.getRboardService(this) {
            rboardService = it

            Log.d("RBOARD", "Rboard service connected")
            Log.d("RBOARD", "Rboard service version: ${it.aidlVersion}")
            Log.d("RBOARD", "Rboard service themes: ${it.rboardThemes.contentToString()}")
        }

        ColorUtilsC.init()

        clearTmp()
        ThemePack.clearCache(this)
        patcher.clearCache()
    }
}