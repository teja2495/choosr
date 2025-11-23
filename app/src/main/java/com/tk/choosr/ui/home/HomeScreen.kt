package com.tk.choosr.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.ui.components.ImportWarningDialog
import com.tk.choosr.ui.shuffle.ShuffleDialog
import com.tk.choosr.util.rememberBackupManager
import com.tk.choosr.viewmodel.ListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ListsViewModel,
    snackbarHostState: SnackbarHostState,
    onCreateList: () -> Unit,
    onEditList: (String) -> Unit,
    onShuffle: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val lists by viewModel.lists.collectAsState()
    var showShuffleDrawer by remember { mutableStateOf(false) }
    var selectedList by remember { mutableStateOf<ChoiceList?>(null) }
    var listToDelete by remember { mutableStateOf<ChoiceList?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showImportWarning by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    val backupManager = rememberBackupManager(viewModel, snackbarHostState)

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
                backupManager.importBackup(scope, it, logTag = "HomeScreen")
            } else {
                // Show warning if there are existing lists
                pendingImportUri = it
                showImportWarning = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(top = 8.dp),
        containerColor = Color.Black
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(inner)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom title with settings icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, bottom = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Choosr",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                if (lists.isEmpty()) {
                    // Empty state placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(start = 14.dp, top = 150.dp, end = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No lists yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            val annotatedText = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                                    append("Add a new list using the floating button below")
                                }
                                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                                    append(" or ")
                                }
                                pushStringAnnotation(tag = "import", annotation = "import")
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.White,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append("import")
                                }
                                pop()
                                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                                    append(" if you already have a backup file.")
                                }
                            }
                            ClickableText(
                                text = annotatedText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textAlign = TextAlign.Center
                                ),
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations(
                                        tag = "import",
                                        start = offset,
                                        end = offset
                                    ).firstOrNull()?.let {
                                        // Open file picker - accept any file type
                                        importLauncher.launch(arrayOf("*/*"))
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lists, key = { it.id }) { list ->
                            ListCard(
                                list = list,
                                onShuffle = { 
                                    selectedList = list
                                    showShuffleDrawer = true
                                },
                                onEdit = { onEditList(list.id) },
                                onLongPress = { listToDelete = list }
                            )
                        }
                    }
                }
            }

            // Shuffle dialog
            if (showShuffleDrawer && selectedList != null) {
                ShuffleDialog(
                    list = selectedList,
                    viewModel = viewModel,
                    onDismiss = { showShuffleDrawer = false }
                )
            }

            // Delete Confirmation Dialog
            listToDelete?.let { list ->
                AlertDialog(
                    onDismissRequest = { listToDelete = null },
                    title = { 
                        Text(
                            "Delete List?",
                            color = Color.White
                        ) 
                    },
                    text = {
                        Text(
                            "Are you sure you want to delete \"${list.name}\"? This action cannot be undone.",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    containerColor = Color(0xFF1E1E1E),
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteList(list.id)
                                listToDelete = null
                            }
                        ) {
                            Text("Delete", color = Color(0xFFFF6B6B))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToDelete = null }) {
                            Text("Cancel", color = Color.White)
                        }
                    }
                )
            }

            // Import warning dialog
            ImportWarningDialog(
                visible = showImportWarning,
                onConfirm = {
                    pendingImportUri?.let { uri ->
                        backupManager.importBackup(scope, uri, logTag = "HomeScreen")
                    }
                    showImportWarning = false
                    pendingImportUri = null
                },
                onDismiss = {
                    showImportWarning = false
                    pendingImportUri = null
                }
            )

            // Floating Action Button positioned manually
            ExtendedFloatingActionButton(
                onClick = onCreateList,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add List",
                        tint = Color.Black
                    )
                },
                text = { Text("New List", color = Color.Black) },
                containerColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (-48).dp)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ListCard(
    list: ChoiceList,
    onShuffle: () -> Unit,
    onEdit: () -> Unit,
    onLongPress: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onEdit() },
                    onLongPress = { onLongPress() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = list.colorArgb?.let { Color(it) } ?: Color(0xFF1F1F1F),
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Column {
                val title = buildString {
                    if (!list.emoji.isNullOrBlank()) append(list.emoji + " ")
                    append(list.name)
                }
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${list.items.size} items", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = onShuffle, 
                enabled = list.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { 
                Icon(
                    painter = painterResource(id = com.tk.choosr.R.drawable.ic_shuffle),
                    contentDescription = "Shuffle",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}


