package de.dertyp7214.rboardpatcher.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.file.forceDelete
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import de.dertyp7214.rboardcomponents.components.SearchBar
import de.dertyp7214.rboardpatcher.Application
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.PatchAdapter
import de.dertyp7214.rboardpatcher.adapter.PatchInfoIconAdapter
import de.dertyp7214.rboardpatcher.api.GitHub
import de.dertyp7214.rboardpatcher.api.types.KeyValue
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.components.BaseActivity
import de.dertyp7214.rboardpatcher.components.ChipContainer
import de.dertyp7214.rboardpatcher.core.Observe
import de.dertyp7214.rboardpatcher.core.app
import de.dertyp7214.rboardpatcher.core.decodeBitmap
import de.dertyp7214.rboardpatcher.core.dp
import de.dertyp7214.rboardpatcher.core.get
import de.dertyp7214.rboardpatcher.core.openDialog
import de.dertyp7214.rboardpatcher.core.openShareThemeDialog
import de.dertyp7214.rboardpatcher.core.parseThemeDataClass
import de.dertyp7214.rboardpatcher.core.preferences
import de.dertyp7214.rboardpatcher.core.toRboardTheme
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
import kotlin.collections.set
import kotlin.math.roundToInt

@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class PatchActivity : BaseActivity() {

    private val mutableLiveBitmap = MutableLiveData<Bitmap?>()

    private val imagePickerResultLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val bitmap =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    }

                mutableLiveBitmap.value = bitmap
            }
        }

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
            openDialog(R.layout.patch_info_popup, true) { dialog ->
                val list = arrayListOf<Pair<String, Bitmap?>>()
                val adapter = PatchInfoIconAdapter(this@PatchActivity, list)

                val progressBar = findViewById<CircularProgressIndicator>(R.id.progressBar)

                val okButton = findViewById<Button>(R.id.ok)

                val tags = findViewById<TextView>(R.id.tags)
                val title = findViewById<TextView>(R.id.title)
                val message = findViewById<TextView>(R.id.message)

                val fontPreview = findViewById<TextView>(R.id.fontPreview)

                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.layoutManager = GridLayoutManager(this@PatchActivity, 1)
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = adapter

                tags.text = patchMeta.tags.joinToString(",")
                title.text = patchMeta.name
                message.text = patchMeta.description ?: "No Description!"

                tags.requestFocus()

                okButton.setOnClickListener { dialog.dismiss() }

                if (patchMeta.tags.any { it.startsWith("icon", true) }) {
                    progressBar.visibility = VISIBLE
                    doAsync({
                        val path = Patch(patchMeta).getPatches(
                            this@PatchActivity,
                            File(context.cacheDir, "preview").also { it.forceDelete(true) }
                        )
                        path.listFiles { file -> file.extension == "png" }?.map { file ->
                            Pair(
                                file.nameWithoutExtension.removePrefix("icon_"),
                                file.decodeBitmap()
                            )
                        }
                    }) { pairList ->
                        progressBar.visibility = GONE
                        if (pairList != null) {
                            recyclerView.visibility = VISIBLE
                            list.clear()
                            list.addAll(pairList.toList())
                            (recyclerView.layoutManager as GridLayoutManager).spanCount =
                                measuredWidth.let { if (it > 0) (it.toFloat() / 80.dp(this@PatchActivity)).roundToInt() else 3 }
                            adapter.notifyDataSetChanged()
                        }
                    }
                } else if (patchMeta.tags.any { it.startsWith("font", true) }) {
                    fontPreview.visibility = VISIBLE
                    fontPreview.typeface = Typeface.create(patchMeta.font, Typeface.NORMAL)
                }
            }
        }) { patches, patchMeta, selected ->
            patchTheme.isEnabled = patches.isNotEmpty() && managerInstalled
            shareTheme.isEnabled = patches.isNotEmpty()

            if (!selected) return@PatchAdapter

            if (
                patchMeta.tags.any { it.equals("custom", true) }
                && patchMeta.tags.any { it.equals("image", true) }
            ) {
                openDialog(R.layout.custom_image_patch_layout, false) { dialog ->
                    val title = findViewById<TextView>(R.id.title)
                    val tags = findViewById<TextView>(R.id.tags)
                    val message = findViewById<TextView>(R.id.message)
                    val image = findViewById<ImageView>(R.id.image)
                    val imageDescription = findViewById<TextView>(R.id.imageDescription)

                    val replaceImageButton = findViewById<MaterialButton>(R.id.replaceImageButton)
                    val positiveButton = findViewById<Button>(R.id.ok)

                    replaceImageButton.isEnabled = false

                    val patch = Patch(patchMeta)

                    var customImage: Bitmap? = null

                    val observer = Observe { bitmap ->
                        if (bitmap != null) {
                            mutableLiveBitmap.value = null
                            mutableLiveBitmap.removeObserver(this)
                            image.setImageBitmap(bitmap)
                            customImage = bitmap
                        }
                    }

                    replaceImageButton.setOnClickListener {
                        mutableLiveBitmap.observe(this@PatchActivity, observer)
                        imagePickerResultLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }

                    positiveButton.setOnClickListener {
                        patchMeta.customImage = customImage?.let {
                            KeyValue(patchMeta.customName ?: "", it)
                        }

                        dialog.dismiss()
                    }

                    title.text = patchMeta.name
                    tags.text = patchMeta.tags.joinToString(",")
                    message.text = patchMeta.description ?: "No Description!"

                    doAsync({
                        val path = patch.getPatches(
                            this@PatchActivity,
                            File(context.cacheDir, "preview").also { it.forceDelete(true) }
                        )
                        path.listFiles { file -> file.extension == "png" }?.map { file ->
                            Pair(
                                file.nameWithoutExtension.removePrefix("icon_"),
                                file.decodeBitmap()
                            )
                        }
                    }) { pairList ->
                        if (pairList?.size == 1) {
                            image.setImageBitmap(pairList[0].second)
                            imageDescription.text = pairList[0].first

                            replaceImageButton.isEnabled = true
                        } else {
                            image.visibility = GONE
                            imageDescription.text = "No Image or more than one Image found!"
                        }
                    }
                }
            } else if (
                patchMeta.tags.any { it.equals("custom", true) }
                && patchMeta.tags.any { it.equals("value", true) }
            ) {
                openDialog(R.layout.custom_value_patch_layout, false) { dialog ->
                    val title = findViewById<TextView>(R.id.title)
                    val tags = findViewById<TextView>(R.id.tags)
                    val message = findViewById<TextView>(R.id.message)
                    val patchValue = findViewById<EditText>(R.id.patchValue)
                    val positiveButton = findViewById<Button>(R.id.ok)

                    positiveButton.isEnabled = false

                    patchValue.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            positiveButton.isEnabled = s?.isNotEmpty() == true
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }
                    })

                    positiveButton.setOnClickListener {
                        patchMeta.customValue = patchMeta.customName?.let { s ->
                            KeyValue(s, patchValue.text.toString())
                        }

                        dialog.dismiss()
                    }

                    title.text = patchMeta.name
                    tags.text = patchMeta.tags.joinToString(",")
                    message.text = patchMeta.description ?: "No Description!"
                }
            }
        }
    }

    private val resultLauncherMain =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            MainActivity::class.java[this]
            finish()
        }
    private val resultLauncherManager =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val intent =
                managerPackageName?.let(packageManager::getLaunchIntentForPackage) ?: Intent(
                    this,
                    MainActivity::class.java
                )
            startActivity(intent)
            finish()
        }

    private val lastVisit by lazy { preferences.getLong("lastVisit", System.currentTimeMillis()) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.navigationBarColor = Color.TRANSPARENT
        }
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
                    if (a.date > lastVisit && b.date > lastVisit) a.name.compareTo(
                        b.name,
                        true
                    )
                    else if (a.date > lastVisit) -1
                    else if (b.date > lastVisit) 1
                    else if (a.tags.contains("custom") && !b.tags.contains("custom")) -1
                    else if (!a.tags.contains("custom") && b.tags.contains("custom")) 1
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
                            unfiltered, filters, searchFilter
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
                            unfiltered, filters, searchFilter
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

        app.patcher.patchTheme(theme,
            toRemove,
            *selected.map { Patch(it) }.toTypedArray(),
            progress = { progress, stage ->
                CoroutineScope(Dispatchers.Main).launch {
                    patchTheme.text = "Applying: $stage"
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        progressBar.setProgress(progress.roundToInt(), true)
                    } else {
                        progressBar.progress = progress.roundToInt()
                    }
                }
            }) {
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

                if (install && Application.rboardService != null) {
                    val result = Application.rboardService?.installRboardTheme(
                        themeFile.absolutePath.parseThemeDataClass().toRboardTheme()
                    )

                    if (result == true) {
                        Toast.makeText(
                            this@PatchActivity,
                            getString(R.string.theme_installed),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@PatchActivity,
                            getString(R.string.theme_install_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
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
                }
                dialogInterface.dismiss()
            }
        }
    }

    private fun filterPatches(
        unfiltered: List<PatchMeta>, filters: List<String>, searchFilter: String
    ): List<PatchMeta> {
        return unfiltered.filter { patch ->
            (patch.name.contains(searchFilter, true) || patch.author.contains(
                searchFilter, true
            )) && (filters.isEmpty() || patch.tags.any { filters.contains(it) })
        }
    }
}