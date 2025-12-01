package com.example.gestorarchivosipn

import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: FileViewModel,
    onFileClick: (File) -> Unit
) {
    val files = viewModel.fileList
    val currentPath = viewModel.currentPath
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestor IPN/ESCOM", style = MaterialTheme.typography.titleMedium)
                        Text(currentPath.name, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(Icons.Default.ColorLens, "Cambiar Tema")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Breadcrumb simplificado
            Text(
                text = currentPath.absolutePath.replace("/storage/emulated/0", "Alm. Interno"),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                fontSize = 12.sp
            )

            if (files.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carpeta vacía")
                }
            } else {
                LazyColumn {
                    items(files) { file ->
                        val isFav = favorites.any { it.path == file.absolutePath }
                        FileItem(
                            file = file,
                            isFavorite = isFav,
                            details = viewModel.getFileDetails(file),
                            onClick = { onFileClick(file) },
                            onLongClick = { /* Implementar menú contextual: Eliminar/Renombrar */ },
                            onFavClick = {
                                if (isFav) viewModel.removeFavorite(file.absolutePath)
                                else viewModel.addFavorite(file)
                            },
                            onDelete = { viewModel.deleteFile(file) }
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    file: File,
    isFavorite: Boolean,
    details: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Outlined.Folder else Icons.Outlined.Description,
            contentDescription = null,
            tint = if (file.isDirectory) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = details, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        IconButton(onClick = onFavClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorito",
                tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
            )
        }

        // Dropdown Menu para eliminar (activado con long click)
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Eliminar") },
                onClick = {
                    onDelete()
                    showMenu = false
                }
            )
        }
    }
}

// --- VISUALIZADORES ---

@Composable
fun TextFileViewer(file: File) {
    val text = remember(file) { file.readText() }
    val extension = file.extension.lowercase()

    val displayText = if (extension == "json") {
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val je = JsonParser.parseString(text)
            gson.toJson(je)
        } catch (e: Exception) { text }
    } else {
        text
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(text = displayText, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ImageViewer(file: File) {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(state = state),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(file),
            contentDescription = file.name,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .fillMaxSize()
        )
    }
}