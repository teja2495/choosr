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

class BackupManager(
    private val context: Context,
    private val viewModel: ListsViewModel,
    private val snackbarHostState: SnackbarHostState
) {

    fun importBackup(scope: CoroutineScope, uri: Uri, logTag: String = TAG) {
        scope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    snackbarHostState.showSnackbar("Failed to open file")
                    return@launch
                }

                inputStream.use { stream ->
                    val json = stream.bufferedReader().use { it.readText() }
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
                }
            } catch (e: SecurityException) {
                Log.e(logTag, "Security error importing file", e)
                snackbarHostState.showSnackbar("Permission denied. Please try again.")
            } catch (e: Exception) {
                Log.e(logTag, "Error importing file", e)
                snackbarHostState.showSnackbar("Error importing file: ${e.message}")
            }
        }
    }

    fun exportBackup(scope: CoroutineScope, uri: Uri) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val exportData = viewModel.exportData()
                    outputStream.write(exportData.toByteArray())
                } ?: error("Failed to open export destination")
            }.onFailure { throwable ->
                Log.e(TAG, "Error exporting backup", throwable)
            }
        }
    }

    companion object {
        private const val TAG = "BackupManager"
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

