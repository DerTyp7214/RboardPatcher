package de.dertyp7214.rboardpatcher

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import de.dertyp7214.rboardpatcher.api.GitHub
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.patcher.Patch
import de.dertyp7214.rboardpatcher.utils.doAsync

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        doAsync({ GitHub.GboardThemes.Patches["patches.json"] }) { list: List<PatchMeta> ->
            Patch(list.last()).getPatches(this) {
                Log.d("REEEEE", it.absolutePath)
            }
        }
    }
}