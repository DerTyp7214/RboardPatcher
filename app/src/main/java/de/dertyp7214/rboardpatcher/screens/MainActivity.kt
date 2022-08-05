package de.dertyp7214.rboardpatcher.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.callback.FilePickerCallback
import com.anggrayudi.storage.file.openInputStream
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.core.set
import java.io.File

class MainActivity : AppCompatActivity() {

    private val storage = SimpleStorage(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage.filePickerCallback = object : FilePickerCallback {
            override fun onStoragePermissionDenied(requestCode: Int, files: List<DocumentFile>?) {}
            override fun onCanceledByUser(requestCode: Int) {
                finish()
            }
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
                finish()
            }
        }

        storage.openFilePicker(allowMultiple = false)
    }
}