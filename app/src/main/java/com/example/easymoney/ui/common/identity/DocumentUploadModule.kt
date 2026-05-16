package com.example.easymoney.ui.common.identity

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.easymoney.R
import java.io.File

/**
 * Shared Module for Document Upload (Camera & File Picker)
 */
@Composable
fun DocumentUploadModule(
    onResult: (DocumentResult) -> Unit,
    onDismiss: () -> Unit,
    onError: (String) -> Unit,
    maxFileSizeBytes: Long = 5 * 1024 * 1024 // Default 5MB
) {
    val context = LocalContext.current

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileInfo = getFileInfo(context, it)
            if (fileInfo.size > maxFileSizeBytes) {
                onError("File quá lớn. Vui lòng chọn file dưới ${maxFileSizeBytes / (1024 * 1024)}MB")
            } else {
                onResult(
                    DocumentResult(
                        fileUri = it,
                        fileName = fileInfo.name,
                        fileSize = fileInfo.size,
                        mimeType = fileInfo.mimeType,
                        isFromCamera = false
                    )
                )
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // In a real app, save to file and get Uri
            // For now, return mock result
            onResult(DocumentResult(null, "camera_capture.jpg", 1024, "image/jpeg", true))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.document_upload_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.document_upload_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UploadOptionButton(
                        icon = Icons.Default.CameraAlt,
                        label = stringResource(R.string.document_upload_camera),
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f)
                    )
                    UploadOptionButton(
                        icon = Icons.Default.PhotoLibrary,
                        label = stringResource(R.string.document_upload_gallery),
                        onClick = { filePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.document_upload_cancel))
                }
            }
        }
    }
}

@Composable
private fun UploadOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private data class FileMetaData(val name: String, val size: Long, val mimeType: String)

private fun getFileInfo(context: android.content.Context, uri: Uri): FileMetaData {
    var name = "document"
    var size = 0L
    var mimeType = "application/octet-stream"
    
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
            size = cursor.getLong(sizeIndex)
        }
    }
    mimeType = context.contentResolver.getType(uri) ?: mimeType
    
    return FileMetaData(name, size, mimeType)
}
