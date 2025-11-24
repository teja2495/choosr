package com.tk.choosr.ui.settings

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tk.choosr.ui.components.ImportWarningDialog
import com.tk.choosr.util.rememberBackupManager
import com.tk.choosr.viewmodel.ListsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ListsViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val avoidPreviousResults by viewModel.avoidPreviousResults.collectAsState()
    val viewType by viewModel.viewType.collectAsState()
    val lists by viewModel.lists.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = rememberBackupManager(viewModel, snackbarHostState)
    var showImportWarning by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            backupManager.exportBackup(scope, it)
        }
    }

    fun getExportFileName(): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        return "choosr_backup_$timestamp.choosr"
    }

    // Import launcher - accept any file, validate during import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Take persistable URI permission so we can read the file later
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // If we can't take persistable permission, still try to import
                // Some file pickers don't support this
            }
            
            // If there are no existing lists, import directly without warning
            if (lists.isEmpty()) {
                backupManager.importBackup(scope, it, logTag = "SettingsScreen")
            } else {
                // Show warning if there are existing lists
                pendingImportUri = it
                showImportWarning = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1F1F1F),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Avoid Previous Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "When enabled, previously chosen results won't appear again until all items have been chosen",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Switch(
                        checked = avoidPreviousResults,
                        onCheckedChange = { viewModel.setAvoidPreviousResults(it) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1F1F1F),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "List View",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Display lists as rows instead of square cards",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Switch(
                        checked = viewType == "list",
                        onCheckedChange = { 
                            viewModel.setViewType(if (it) "list" else "grid")
                        }
                    )
                }

                // Import button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1F1F1F),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            // Open file picker - accept any file type
                            importLauncher.launch(arrayOf("*/*"))
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Import Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Import data from a .choosr backup file",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Export button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1F1F1F),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            exportLauncher.launch(getExportFileName())
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Export Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Export all your lists and settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    ImportWarningDialog(
        visible = showImportWarning,
        onConfirm = {
            pendingImportUri?.let { uri ->
                backupManager.importBackup(scope, uri, logTag = "SettingsScreen")
            }
            showImportWarning = false
            pendingImportUri = null
        },
        onDismiss = {
            showImportWarning = false
            pendingImportUri = null
        }
    )
}

