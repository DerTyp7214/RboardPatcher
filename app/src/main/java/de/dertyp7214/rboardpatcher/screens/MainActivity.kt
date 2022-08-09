package de.dertyp7214.rboardpatcher.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.callback.FilePickerCallback
import com.anggrayudi.storage.file.openInputStream
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.MainOptionAdapter
import de.dertyp7214.rboardpatcher.components.BaseActivity
import de.dertyp7214.rboardpatcher.components.MarginItemDecoration
import de.dertyp7214.rboardpatcher.core.dp
import de.dertyp7214.rboardpatcher.core.openUrl
import de.dertyp7214.rboardpatcher.core.set
import de.dertyp7214.rboardpatcher.screens.types.MainOption
import de.dertyp7214.rboardpatcher.utils.isPackageInstalled
import java.io.File

class MainActivity : BaseActivity() {

    private val storage = SimpleStorage(this)

    private val list by lazy {
        arrayListOf<MainOption>().apply {
            if (managerInstalled) {
                if (isPackageInstalled(managerPackage, packageManager))
                    add(MainOption(R.drawable.ic_launcher_manager, "Open Rboard Manager") {
                        startActivity(packageManager.getLaunchIntentForPackage(managerPackage))
                    })
                if (isPackageInstalled("$managerPackage.debug", packageManager))
                    add(MainOption(R.drawable.ic_launcher_manager, "Open Rboard Manager Debug") {
                        startActivity(packageManager.getLaunchIntentForPackage("$managerPackage.debug"))
                    })
            } else add(MainOption(R.drawable.ic_xda, "Open XDA") {
                openUrl(getString(R.string.xdaThreadUrl))
            })
            add(MainOption(R.drawable.ic_folder, "Open theme from file manager") {
                storage.openFilePicker(allowMultiple = false)
            })
        }
    }
    private val adapter by lazy { MainOptionAdapter(this, list) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerview) }

    private val sourceCode by lazy { findViewById<View>(R.id.sourceCode) }
    private val patchesRepo by lazy { findViewById<View>(R.id.patchesRepo) }
    private val xdaThread by lazy { findViewById<View>(R.id.xdaThread) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage.filePickerCallback = object : FilePickerCallback {
            override fun onStoragePermissionDenied(requestCode: Int, files: List<DocumentFile>?) {}
            override fun onCanceledByUser(requestCode: Int) {}

            override fun onFileSelected(requestCode: Int, files: List<DocumentFile>) {
                val pack = File(cacheDir, "import.pack")
                files.first().openInputStream(this@MainActivity)?.use { input ->
                    pack.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                LoadThemeActivity::class.java[this@MainActivity] = {
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        packageName,
                        pack
                    )
                    action = Intent.ACTION_VIEW
                    setDataAndType(uri, "application/pack")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
        }

        recyclerView.addItemDecoration(MarginItemDecoration(2.dp(this), top = true, bottom = true))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        sourceCode.setOnClickListener { openUrl(getString(R.string.sourceCodeUrl)) }
        patchesRepo.setOnClickListener { openUrl(getString(R.string.patchesRepoUrl)) }
        xdaThread.setOnClickListener { openUrl(getString(R.string.xdaThreadUrl)) }
    }
}