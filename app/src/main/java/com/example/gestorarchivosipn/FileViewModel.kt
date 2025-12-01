package com.example.gestorarchivosipn

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorarchivosipn.data.AppDatabase
import com.example.gestorarchivosipn.data.FavoriteFile
import com.example.gestorarchivosipn.data.HistoryFile
import com.example.gestorarchivosipn.ui.theme.AppThemeType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).fileDao()

    // Estado del tema
    var currentTheme by mutableStateOf(AppThemeType.GUINDA_IPN)
        private set

    fun toggleTheme() {
        currentTheme = if (currentTheme == AppThemeType.GUINDA_IPN)
            AppThemeType.AZUL_ESCOM else AppThemeType.GUINDA_IPN
    }

    // Ruta actual
    var currentPath by mutableStateOf(Environment.getExternalStorageDirectory())
        private set

    var fileList by mutableStateOf<List<File>>(emptyList())
        private set

    // Listas observables de Room
    val favorites = dao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history = dao.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadFiles(currentPath)
    }

    fun navigateTo(file: File) {
        if (file.isDirectory) {
            currentPath = file
            loadFiles(file)
        } else {
            addToHistory(file)
        }
    }

    fun navigateBack(): Boolean {
        val parent = currentPath.parentFile
        // Evitar salir del root del almacenamiento interno (opcional)
        if (parent != null && parent.canRead()) {
            currentPath = parent
            loadFiles(parent)
            return true
        }
        return false
    }

    fun loadFiles(directory: File) {
        viewModelScope.launch {
            try {
                val files = directory.listFiles()
                fileList = files?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?: emptyList()
            } catch (e: Exception) {
                Log.e("FileVM", "Error loading files: ${e.message}")
            }
        }
    }

    // Operaciones DB
    fun toggleFavorite(file: File) {
        viewModelScope.launch {
            val path = file.absolutePath
            // Esto es un toggle simple, en una app real se comprobaría primero
            // Aquí solo agregamos, para simplificar el ejemplo de "Gestor"
            // Implementaremos un check en la UI
        }
    }

    fun addFavorite(file: File) {
        viewModelScope.launch {
            dao.addFavorite(FavoriteFile(file.absolutePath, file.name, file.isDirectory))
        }
    }

    fun removeFavorite(path: String) {
        viewModelScope.launch {
            dao.removeFavorite(path)
        }
    }

    private fun addToHistory(file: File) {
        viewModelScope.launch {
            dao.addToHistory(HistoryFile(file.absolutePath, file.name, System.currentTimeMillis()))
        }
    }

    // Operaciones de Archivo
    fun deleteFile(file: File) {
        try {
            if (file.delete()) {
                loadFiles(currentPath)
            }
        } catch (e: Exception) {
            Log.e("FileVM", "Error deleting: ${e.message}")
        }
    }

    // Utilidades
    fun getFileDetails(file: File): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = sdf.format(Date(file.lastModified()))
        val size = if (file.isDirectory) "${file.listFiles()?.size ?: 0} items"
        else "${file.length() / 1024} KB"
        return "$date | $size"
    }

    fun getMimeType(file: File): String? {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }
}