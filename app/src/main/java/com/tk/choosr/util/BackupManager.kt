package com.tk.choosr.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tk.choosr.viewmodel.ListsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.io.DEFAULT_BUFFER_SIZE
import kotlin.text.Charsets

class BackupManager(
    private val context: Context,
    private val viewModel: ListsViewModel,
    private val snackbarHostState: SnackbarHostState
) {

    fun importBackup(scope: CoroutineScope, uri: Uri, logTag: String = TAG) {
        scope.launch {
            val readResult = withContext(Dispatchers.IO) {
                runCatching { readBackupFile(uri) }
            }

            readResult.onSuccess { json ->
                if (json.isBlank()) {
                    snackbarHostState.showSnackbar("File is empty")
                    return@launch
                }
                val success = viewModel.importData(json)
                if (success) {
                    snackbarHostState.showSnackbar("Data imported successfully")
                } else {
                    snackbarHostState.showSnackbar("Invalid file format")
                }
            }.onFailure { throwable ->
                when (throwable) {
                    is SecurityException -> {
                        Log.e(logTag, "Security error importing file", throwable)
                        snackbarHostState.showSnackbar("Permission denied. Please try again.")
                    }
                    is IllegalArgumentException -> {
                        Log.e(logTag, "Invalid backup file", throwable)
                        snackbarHostState.showSnackbar(throwable.message ?: "Invalid backup file")
                    }
                    else -> {
                        Log.e(logTag, "Error importing file", throwable)
                        snackbarHostState.showSnackbar("Error importing file")
                    }
                }
            }
        }
    }

    fun exportBackup(scope: CoroutineScope, uri: Uri) {
        scope.launch {
            val exportResult = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val exportData = viewModel.exportData()
                        outputStream.write(exportData.toByteArray())
                    } ?: error("Failed to open export destination")
                }
            }

            exportResult.onSuccess {
                snackbarHostState.showSnackbar("Backup exported successfully")
            }.onFailure { throwable ->
                Log.e(TAG, "Error exporting backup", throwable)
                snackbarHostState.showSnackbar("Error exporting backup")
            }
        }
    }

    private fun readBackupFile(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Failed to open selected file")

        return inputStream.use { stream ->
            val buffer = ByteArrayOutputStream()
            val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
            var totalBytes = 0
            var read: Int

            while (true) {
                read = stream.read(chunk)
                if (read == -1) break
                totalBytes += read
                if (totalBytes > MAX_IMPORT_BYTES) {
                    throw IllegalArgumentException("Backup file is too large")
                }
                buffer.write(chunk, 0, read)
            }

            if (totalBytes == 0) {
                throw IllegalArgumentException("File is empty")
            }

            buffer.toString(Charsets.UTF_8.name())
        }
    }

    companion object {
        private const val TAG = "BackupManager"
        private const val MAX_IMPORT_BYTES = 512 * 1024 // 512 KB safety limit
    }
}

@Composable
fun rememberBackupManager(
    viewModel: ListsViewModel,
    snackbarHostState: SnackbarHostState
): BackupManager {
    val context = LocalContext.current
    return remember(context, viewModel, snackbarHostState) {
        BackupManager(context, viewModel, snackbarHostState)
    }
}

