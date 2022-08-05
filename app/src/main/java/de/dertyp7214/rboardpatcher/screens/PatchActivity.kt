package de.dertyp7214.rboardpatcher.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.PatchAdapter
import de.dertyp7214.rboardpatcher.api.GitHub
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.components.ChipContainer
import de.dertyp7214.rboardpatcher.components.SearchBar
import de.dertyp7214.rboardpatcher.core.app
import de.dertyp7214.rboardpatcher.core.get
import de.dertyp7214.rboardpatcher.core.openShareThemeDialog
import de.dertyp7214.rboardpatcher.core.parseThemeDataClass
import de.dertyp7214.rboardpatcher.patcher.Patch
import de.dertyp7214.rboardpatcher.patcher.Theme
import de.dertyp7214.rboardpatcher.utils.ThemeUtils
import de.dertyp7214.rboardpatcher.utils.ZipHelper
import de.dertyp7214.rboardpatcher.utils.doAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

class PatchActivity : AppCompatActivity() {

    private val unfiltered = arrayListOf<PatchMeta>()
    private val list = arrayListOf<PatchMeta>()

    private val progressBar by lazy { findViewById<LinearProgressIndicator>(R.id.progressBar) }
    private val patchTheme by lazy { findViewById<MaterialButton>(R.id.patchTheme) }
    private val shareTheme by lazy { findViewById<MaterialButton>(R.id.shareButton) }
    private val searchBar by lazy { findViewById<SearchBar>(R.id.searchBar) }
    private val chipContainer by lazy { findViewById<ChipContainer>(R.id.chipContainer) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerview) }
    private val adapter by lazy {
        PatchAdapter(this, list) {
            patchTheme.isEnabled = it.isNotEmpty()
            shareTheme.isEnabled = it.isNotEmpty()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patch)

        val themeDataClass = intent.getStringExtra("themePath")?.parseThemeDataClass()

        if (themeDataClass != null) {
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(this)

            doAsync({
                val patches: List<PatchMeta> = GitHub.GboardThemes.Patches["patches.json"]
                val theme = themeDataClass.getTheme(this)
                Pair(patches, theme)
            }) { (patches, theme) ->
                unfiltered.clear()
                unfiltered.addAll(patches)
                list.clear()
                list.addAll(unfiltered)
                adapter.notifyDataChanged()

                val tags = arrayListOf<String>()
                list.forEach {
                    it.tags.forEach { tag -> if (!tags.contains(tag)) tags.add(tag) }
                }
                chipContainer.setChips(tags)

                patchTheme.setOnClickListener {
                    patchTheme(theme, true)
                }

                shareTheme.setOnClickListener {
                    patchTheme(theme, false)
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
                    adapter.unselectAll()
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
                    adapter.unselectAll()
                    adapter.notifyDataChanged()
                }
            }
        }
    }

    private fun patchTheme(theme: Theme, install: Boolean) {
        patchTheme.isEnabled = false
        shareTheme.isEnabled = false
        adapter.isEnabled = false
        progressBar.progress = 0
        progressBar.visibility = VISIBLE
        app.patcher.patchTheme(
            theme,
            *adapter.getSelected().map { Patch(it) }.toTypedArray(),
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
            }) { dialogInterface, name, author ->
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

                ThemeUtils.shareTheme(this, pack, install)
                dialogInterface.dismiss()
                MainActivity::class.java[this@PatchActivity]
                finish()
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