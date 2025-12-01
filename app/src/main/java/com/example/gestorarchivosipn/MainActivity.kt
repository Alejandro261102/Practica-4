package com.example.gestorarchivosipn

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestorarchivosipn.ui.theme.GestorArchivosIPNTheme
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: FileViewModel by viewModels()

    // Gestión de permisos para Android 11+
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Environment.isExternalStorageManager()) {
            viewModel.loadFiles(Environment.getExternalStorageDirectory())
        }
    }

    // Gestión permisos Android < 11
    private val legacyPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.loadFiles(Environment.getExternalStorageDirectory())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        setContent {
            GestorArchivosIPNTheme(themeType = viewModel.currentTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }

    @Composable
    fun AppNavigation() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "explorer") {
            composable("explorer") {
                // Manejo del botón atrás manual para subir de carpeta
                androidx.activity.compose.BackHandler {
                    if (!viewModel.navigateBack()) {
                        finish() // Salir si estamos en raíz
                    }
                }

                FileExplorerScreen(
                    viewModel = viewModel,
                    onFileClick = { file ->
                        if (file.isDirectory) {
                            viewModel.navigateTo(file)
                        } else {
                            handleFileOpening(file, navController)
                        }
                    }
                )
            }

            composable("viewer/{filePath}") { backStackEntry ->
                val pathEncoded = backStackEntry.arguments?.getString("filePath") ?: ""
                val path = java.net.URLDecoder.decode(pathEncoded, StandardCharsets.UTF_8.toString())
                val file = File(path)

                if (file.extension.lowercase() in listOf("jpg", "png", "jpeg")) {
                    ImageViewer(file)
                } else {
                    TextFileViewer(file)
                }
            }
        }
    }

    private fun handleFileOpening(file: File, navController: androidx.navigation.NavController) {
        val ext = file.extension.lowercase()

        // Archivos soportados internamente
        if (ext in listOf("txt", "md", "json", "xml", "html", "java", "kt", "jpg", "png", "jpeg")) {
            val encodedPath = URLEncoder.encode(file.absolutePath, StandardCharsets.UTF_8.toString())
            navController.navigate("viewer/$encodedPath")
        } else {
            // Intent externo "Open With"
            openFileExternally(file)
        }
    }

    private fun openFileExternally(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            val mime = viewModel.getMimeType(file) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Abrir con..."))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No hay app para abrir este archivo", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                storagePermissionLauncher.launch(intent)
            }
        } else {
            // Android 10 e inferiores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                legacyPermissionLauncher.launch(arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
            }
        }
    }
}