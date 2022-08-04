package de.dertyp7214.rboardpatcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.dertyp7214.rboardpatcher.api.GitHub
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.core.clearTmp
import de.dertyp7214.rboardpatcher.patcher.Patch
import de.dertyp7214.rboardpatcher.patcher.Patcher
import de.dertyp7214.rboardpatcher.patcher.ThemePack
import de.dertyp7214.rboardpatcher.utils.doInBackground

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val patcher = Patcher(this)

        clearTmp()
        ThemePack.clearCache(this)
        patcher.clearCache()

        doInBackground {
            val list: List<PatchMeta> = GitHub.GboardThemes.Patches["patches.json"]
            val themes = ThemePack(
                this,
                "https://github.com/GboardThemes/RboardCommunityThemes/raw/main/packs/DerTyp7214.pack"
            ).getThemes()
            themes.forEach { theme ->
                patcher.patchTheme(theme, *list.map { Patch(it) }.toTypedArray(), clean = false)
            }
        }
    }
}