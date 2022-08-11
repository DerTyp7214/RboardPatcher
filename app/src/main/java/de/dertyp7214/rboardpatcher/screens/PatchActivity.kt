package de.dertyp7214.rboardpatcher.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.PatchAdapter
import de.dertyp7214.rboardpatcher.api.GitHub
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.components.BaseActivity
import de.dertyp7214.rboardpatcher.components.ChipContainer
import de.dertyp7214.rboardpatcher.components.SearchBar
import de.dertyp7214.rboardpatcher.core.*
import de.dertyp7214.rboardpatcher.patcher.Patch
import de.dertyp7214.rboardpatcher.patcher.Theme
import de.dertyp7214.rboardpatcher.patcher.types.FileMap
import de.dertyp7214.rboardpatcher.utils.ThemeUtils
import de.dertyp7214.rboardpatcher.utils.ZipHelper
import de.dertyp7214.rboardpatcher.utils.doAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

class PatchActivity : BaseActivity() {

    private val unfiltered = arrayListOf<PatchMeta>()
    private val list = arrayListOf<PatchMeta>()

    private val progressBar by lazy { findViewById<LinearProgressIndicator>(R.id.progressBar) }
    private val patchTheme by lazy { findViewById<MaterialButton>(R.id.patchTheme) }
    private val shareTheme by lazy { findViewById<MaterialButton>(R.id.shareButton) }
    private val searchBar by lazy { findViewById<SearchBar>(R.id.searchBar) }
    private val chipContainer by lazy { findViewById<ChipContainer>(R.id.chipContainer) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerview) }
    private val adapter by lazy {
        PatchAdapter(this, list, unfiltered, { patchMeta ->
            openDialog(
                patchMeta.description ?: "No Description!",
                "Description",
                getString(android.R.string.ok),
                getString(android.R.string.cancel)
            ) {
                it.dismiss()
            }
        }) {
            patchTheme.isEnabled = it.isNotEmpty() && managerInstalled
            shareTheme.isEnabled = it.isNotEmpty()
        }
    }

    private val resultLauncherMain =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            MainActivity::class.java[this]
            finish()
        }
    private val resultLauncherManager =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val intent = managerPackageName?.let(packageManager::getLaunchIntentForPackage)
                ?: Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    private val lastVisit by lazy { preferences.getLong("lastVisit", System.currentTimeMillis()) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patch)

        preferences.edit {
            putLong("previousVisit", lastVisit)
            putLong("lastVisit", System.currentTimeMillis())
        }

        val themeDataClass = intent.getStringExtra("themePath")?.parseThemeDataClass()

        if (themeDataClass != null) {
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(this)

            doAsync({
                val patches: List<PatchMeta> = GitHub.GboardThemes.Patches["patches.json"]
                val theme = themeDataClass.getTheme(this)
                val filesMap = app.patcher.preparePatch(theme)?.let {
                    it.find { file -> file.name == "fileMap.json" }?.readText()
                        ?.let { text -> Gson().fromJson(text, FileMap::class.java) }
                }
                Triple(patches, theme, filesMap)
            }) { (patches, theme, filesMap) ->
                unfiltered.clear()
                unfiltered.addAll(patches.sortedWith { a, b ->
                    if (a.date > lastVisit && b.date > lastVisit) a.name.compareTo(b.name, true)
                    else if (a.date > lastVisit) -1
                    else if (b.date > lastVisit) 1
                    else a.name.compareTo(b.name, true)
                })
                list.clear()
                list.addAll(unfiltered)
                adapter.notifyDataChanged()

                filesMap?.patches?.let(adapter::select)

                val tags = arrayListOf<String>()
                list.forEach {
                    it.tags.forEach { tag -> if (!tags.contains(tag)) tags.add(tag) }
                }
                chipContainer.setChips(tags)

                patchTheme.setOnClickListener {
                    patchTheme(theme, filesMap ?: FileMap(), true)
                }

                shareTheme.setOnClickListener {
                    patchTheme(theme, filesMap ?: FileMap(), false)
                }
            }

            searchBar.instantSearch = true
            searchBar.setOnSearchListener { searchFilter ->
                val filters = chipContainer.filters
                synchronized(list) {
                    list.clear()
                    list.addAll(
                        filterPatches(
                            unfiltered,
                            filters,
                            searchFilter
                        )
                    )
                    adapter.notifyDataChanged()
                }
            }

            chipContainer.setOnFilterToggle { filters ->
                val searchFilter = searchBar.text
                synchronized(list) {
                    list.clear()
                    list.addAll(
                        filterPatches(
                            unfiltered,
                            filters,
                            searchFilter
                        )
                    )
                    adapter.notifyDataChanged()
                }
            }
        }
    }

    private fun patchTheme(theme: Theme, preselected: FileMap, install: Boolean) {
        patchTheme.isEnabled = false
        shareTheme.isEnabled = false
        adapter.isEnabled = false
        progressBar.progress = 0
        progressBar.visibility = VISIBLE
        val selected = adapter.getSelected()
        val toRemove = hashMapOf<String, List<String>>().apply {
            preselected.patches.forEach { patch ->
                if (selected.find { it.getSafeName() == patch } == null) this[patch] =
                    preselected.patchFiles[patch] ?: listOf()
            }
        }
        app.patcher.patchTheme(
            theme,
            toRemove,
            *selected.map { Patch(it) }.toTypedArray(),
            progress = { progress, stage ->
                CoroutineScope(Dispatchers.Main).launch {
                    patchTheme.text = "Applying: $stage"
                    progressBar.setProgress(progress.roundToInt(), true)
                }
            }
        ) {
            patchTheme.isEnabled = true
            shareTheme.isEnabled = true
            adapter.isEnabled = true
            progressBar.visibility = GONE

            patchTheme.setText(R.string.addToManager)

            openShareThemeDialog(negative = {
                it.dismiss()
                MainActivity::class.java[this@PatchActivity]
                finish()
            }) { dialogInterface, n, a ->
                val name = n.ifBlank { theme.name }
                val author = a.ifBlank { "DerTyp7214" }
                val themeFile = File(it.first.parentFile, "${name.replace(" ", "_")}.zip")
                val imageFile = File(themeFile.absolutePath.removeSuffix(".zip"))
                it.first.renameTo(themeFile)
                it.second?.renameTo(imageFile)
                val files = arrayListOf(themeFile)
                it.second?.let { files.add(imageFile) }

                File(themeFile.parentFile, "pack.meta").apply {
                    files.add(this)
                    writeText("name=$name\nauthor=$author\n")
                }

                val pack = File(themeFile.parentFile, "pack.pack")
                ZipHelper().zip(files.map { file -> file.absolutePath }, pack.absolutePath)

                ThemeUtils.shareTheme(
                    this,
                    pack,
                    install,
                    if (install) resultLauncherManager else resultLauncherMain
                )
                dialogInterface.dismiss()
            }
        }
    }

    private fun filterPatches(
        unfiltered: List<PatchMeta>,
        filters: List<String>,
        searchFilter: String
    ): List<PatchMeta> {
        return unfiltered.filter { patch ->
            (patch.name.contains(searchFilter, true) || patch.author.contains(
                searchFilter,
                true
            )) && (filters.isEmpty() || patch.tags.any { filters.contains(it) })
        }
    }
}