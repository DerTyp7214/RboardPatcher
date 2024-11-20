package de.dertyp7214.rboardpatcher.screens

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dertyp7214.rboardcomponents.utils.ThemeUtils
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.ThemeAdapter
import de.dertyp7214.rboardpatcher.adapter.types.ThemeDataClass
import de.dertyp7214.rboardpatcher.components.MarginItemDecoration
import de.dertyp7214.rboardpatcher.core.dp
import de.dertyp7214.rboardpatcher.core.parseThemeDataClass
import de.dertyp7214.rboardpatcher.core.set
import de.dertyp7214.rboardpatcher.core.writeToFile
import de.dertyp7214.rboardpatcher.utils.ZipHelper
import de.dertyp7214.rboardpatcher.utils.doAsync
import java.io.File

class LoadThemeActivity : AppCompatActivity() {

    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerview) }
    private val themes = arrayListOf<ThemeDataClass>()
    private val adapter by lazy {
        ThemeAdapter(this, themes, this::openPatchActivity)
    }

    private fun openPatchActivity(themeDataClass: ThemeDataClass) {
        ThemeUtils.applyTheme(this) { _, _ ->
            PatchActivity::class.java[this] = {
                putExtra("themePath", themeDataClass.path)
            }
            finish()
        }
    }

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
        setContentView(R.layout.activity_load_theme)

        val scheme = intent.scheme
        val data = intent.data

        val importPath = File(cacheDir, "import")
        if (importPath.exists()) importPath.deleteRecursively()

        importPath.mkdirs()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginItemDecoration(2.dp(this), all = true))

        when {
            scheme != "content" && data != null && data.scheme == "file" -> {
                if (!data.path.isNullOrBlank()) {
                    val file = File(data.path!!).let {
                        File(importPath, "import").apply {
                            it.copyTo(this, true)
                        }
                    }
                    val uri = FileProvider.getUriForFile(this, packageName, file)
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pack")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        startActivity(this)
                        finish()
                    }
                }
            }
            data != null -> {
                doAsync({
                    val zip = File(importPath, "themes.pack").apply {
                        delete()
                        data.writeToFile(this@LoadThemeActivity, this)
                    }
                    if (!zip.exists()) listOf()
                    else {
                        val destination = File(importPath, zip.nameWithoutExtension)
                        File(destination.absolutePath).deleteRecursively()
                        if (ZipHelper().unpackZip(destination.absolutePath, zip.absolutePath)) {
                            destination.listFiles { file -> file.extension == "zip" }?.map {
                                it.absolutePath
                            } ?: listOf()
                        } else listOf()
                    }
                }) { paths ->
                    doAsync({
                        val list = arrayListOf<ThemeDataClass>()
                        paths.forEach { themePath ->
                            list.add(themePath.parseThemeDataClass())
                        }
                        list
                    }) {
                        if (it.size == 1) openPatchActivity(it.first())
                        else {
                            themes.clear()
                            themes.addAll(it)
                            adapter.notifyDataChanged()
                        }
                    }
                }
            }
        }
    }
}