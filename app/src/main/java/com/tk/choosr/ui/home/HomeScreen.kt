package com.tk.choosr.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.viewmodel.ListsViewModel
import com.tk.choosr.ui.shuffle.ShuffleBottomDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ListsViewModel,
    snackbarHostState: SnackbarHostState,
    onCreateList: () -> Unit,
    onEditList: (String) -> Unit,
    onShuffle: (String) -> Unit,
) {
    val lists = viewModel.lists
    var showShuffleDrawer by remember { mutableStateOf(false) }
    var selectedList by remember { mutableStateOf<ChoiceList?>(null) }

    Scaffold(
        floatingActionButton = {
            if (!showShuffleDrawer) {
                FloatingActionButton(onClick = onCreateList) {
                    Icon(Icons.Default.Add, contentDescription = "Add List")
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom title with minimal padding
                Text(
                    text = "Choosr",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lists.value, key = { it.id }) { list ->
                        ListCard(
                            list = list,
                            onShuffle = { 
                                selectedList = list
                                showShuffleDrawer = true
                            },
                            onEdit = { onEditList(list.id) },
                            onDelete = { viewModel.deleteList(list.id) }
                        )
                    }
                }
            }

            // Bottom drawer overlay
            if (showShuffleDrawer && selectedList != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showShuffleDrawer = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ShuffleBottomDrawer(
                        list = selectedList,
                        viewModel = viewModel,
                        onClose = { showShuffleDrawer = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Prevent click through */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun ListCard(
    list: ChoiceList,
    onShuffle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete list?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onEdit() },
                    onLongPress = { confirmDelete = true }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = onShuffle, 
                enabled = list.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("Shuffle") 
            }
        }
    }
}


