package com.tk.choosr.ui.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.viewmodel.ListsViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

private val COLOR_OPTIONS = listOf(
    0xFFD4A017L, // Gold (default)
    0xFFD32F2FL, // Red
    0xFF1976D2L, // Blue
    0xFF388E3CL, // Green
    0xFFF57C00L, // Orange
    0xFF7B1FA2L, // Purple
    0xFF0288D1L, // Light Blue
    0xFF00897BL, // Teal
    0xFFE64A19L, // Deep Orange
    0xFF5C6BC0L, // Indigo
    0xFF558B2FL, // Light Green
    0xFFAD1457L, // Pink
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListScreen(
    viewModel: ListsViewModel,
    listId: String?,
    onDone: () -> Unit,
) {
    val lists by viewModel.lists.collectAsState()
    val existing = lists.firstOrNull { it.id == listId }
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var newItem by remember { mutableStateOf(TextFieldValue()) }
    var items by remember(existing?.id) { mutableStateOf(existing?.items ?: emptyList()) }
    var selectedColor by remember(existing?.id) { 
        mutableStateOf(
            if (existing == null) {
                // Set dark grey as default for new lists
                0xFF1F1F1FL
            } else {
                existing.colorArgb
            }
        )
    }
    var showColorPicker by remember { mutableStateOf(false) }
    var createdListId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Update local items when existing changes (but only if it's actually different)
    LaunchedEffect(existing) {
        existing?.let {
            createdListId = it.id
            if (it.items != items) {
                items = it.items
            }
            if (it.name != name) {
                name = it.name
            }
        }
    }

    // Auto-save functionality
    LaunchedEffect(name, items, selectedColor) {
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty() && items.isNotEmpty()) {
            // Only save if this is actually a change from what's in the ViewModel
            val currentItems = existing?.items ?: emptyList()
            val currentName = existing?.name ?: ""
            val currentColor = existing?.colorArgb
            if (items != currentItems || trimmedName != currentName || selectedColor != currentColor) {
                val list = (existing?.copy(name = trimmedName, items = items, colorArgb = selectedColor))
                    ?: ChoiceList(name = trimmedName, items = items, colorArgb = selectedColor)
                if (existing == null && createdListId == null) {
                    // First time creating this list
                    viewModel.addList(list)
                    createdListId = list.id
                } else if (existing == null && createdListId != null) {
                    // Updating a newly created list (before navigating back)
                    viewModel.updateList(list.copy(id = createdListId!!))
                } else {
                    // Updating existing list
                    viewModel.updateList(list)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (existing == null) "New List" else "Edit List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = "Choose color",
                            tint = when {
                                selectedColor == null || selectedColor == 0xFF1F1F1FL -> Color.White
                                else -> Color(selectedColor!!)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                windowInsets = WindowInsets(top = 8.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(inner)
        ) {
            // Fixed top section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // List name section
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List Name", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Add item section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = newItem,
                            onValueChange = { newItem = it },
                            placeholder = { Text("Enter item name...", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedLabelColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        FloatingActionButton(
                            onClick = {
                                val candidate = newItem.text.trim()
                                if (candidate.isNotEmpty() && items.none { it.equals(candidate, true) }) {
                                    items = items + candidate
                                    newItem = TextFieldValue()
                                }
                            },
                            containerColor = if (newItem.text.trim().isNotEmpty()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add item",
                                tint = if (newItem.text.trim().isNotEmpty())
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Scrollable items list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Items list
                if (items.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "No items yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Add items above to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(items.size, key = { items[it] }) { index ->
                        ItemRow(
                            item = items[index],
                            onDelete = { 
                                items = items.filterNot { it == items[index] }
                                existing?.id?.let { listId ->
                                    viewModel.removeItem(listId, items[index])
                                }
                            }
                        )
                        // Add divider except for last item
                        if (index < items.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
            
            // Items count below the list
            Text(
                text = "${items.size} ${if (items.size == 1) "item" else "items"}",
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = selectedColor,
            onColorSelected = { color ->
                selectedColor = color
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    currentColor: Long?,
    onColorSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Choose a color",
                color = Color.White
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color options grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(COLOR_OPTIONS) { color ->
                        val isSelected = currentColor == color
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(color))
                                .clickable { 
                                    onColorSelected(if (isSelected) null else color)
                                    onDismiss()
                                }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        },
        containerColor = Color.Black,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { 
                    onColorSelected(0xFF1F1F1FL)
                    onDismiss()
                }
            ) {
                Text("Reset to default", color = Color.White)
            }
        }
    )
}

@Composable
private fun ItemRow(
    item: String,
    onDelete: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            animationSpec = tween(300),
            targetOffsetY = { it / 2 }
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


