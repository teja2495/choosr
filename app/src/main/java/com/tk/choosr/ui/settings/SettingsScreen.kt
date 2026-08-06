package com.tk.choosr.ui.settings

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val showResultImmediately by viewModel.showResultImmediately.collectAsState()
    val viewType by viewModel.viewType.collectAsState()
    val lists by viewModel.lists.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = rememberBackupManager(viewModel, snackbarHostState)
    var showImportWarning by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { backupManager.exportBackup(scope, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some document providers do not offer persistable permissions.
            }

            if (lists.isEmpty()) {
                backupManager.importBackup(scope, it, logTag = "SettingsScreen")
            } else {
                pendingImportUri = it
                showImportWarning = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SettingsGroupCard {
                    SettingToggleRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Show Result Immediately",
                        description = "Skip the shuffle animation and show the selected item right away.",
                        checked = showResultImmediately,
                        onCheckedChange = viewModel::setShowResultImmediately
                    )
                    SettingsDivider()
                    SettingToggleRow(
                        icon = Icons.Rounded.History,
                        title = "Avoid Previous Results",
                        description = "Do not repeat an item until every item in the list has been chosen.",
                        checked = avoidPreviousResults,
                        onCheckedChange = viewModel::setAvoidPreviousResults
                    )
                    SettingsDivider()
                    SettingToggleRow(
                        icon = Icons.Rounded.ViewList,
                        title = "List View",
                        description = "Display lists as rows instead of square cards.",
                        checked = viewType == "list",
                        onCheckedChange = { viewModel.setViewType(if (it) "list" else "grid") }
                    )
                }
            }

            item {
                SettingsGroupCard {
                    SettingClickRow(
                        icon = Icons.Rounded.FileUpload,
                        title = "Import Data",
                        description = "Import data from a .choosr backup file.",
                        onClick = { importLauncher.launch(arrayOf("*/*")) }
                    )
                    SettingsDivider()
                    SettingClickRow(
                        icon = Icons.Rounded.FileDownload,
                        title = "Export Data",
                        description = "Export all your lists and settings.",
                        onClick = {
                            val timestamp = SimpleDateFormat(
                                "yyyy-MM-dd_HH-mm-ss",
                                Locale.getDefault()
                            ).format(Date())
                            exportLauncher.launch("choosr_backup_$timestamp.choosr")
                        }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Choosr v$versionName",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    ImportWarningDialog(
        visible = showImportWarning,
        onConfirm = {
            pendingImportUri?.let { backupManager.importBackup(scope, it, logTag = "SettingsScreen") }
            showImportWarning = false
            pendingImportUri = null
        },
        onDismiss = {
            showImportWarning = false
            pendingImportUri = null
        }
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 14.dp),
        color = Color.White.copy(alpha = 0.12f)
    )
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        SettingText(title, description, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingClickRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        SettingText(title, description, Modifier.weight(1f))
    }
}

@Composable
private fun SettingText(title: String, description: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
