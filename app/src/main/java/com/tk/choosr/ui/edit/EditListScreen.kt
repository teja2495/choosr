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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalDensity
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.viewmodel.ListsViewModel

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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    var isTitleFocused by remember { mutableStateOf(false) }
    var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var showInputSection by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var inputKeyboardAppeared by remember { mutableStateOf(false) }
    val inputFocusRequester = remember { FocusRequester() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var navigationJob by remember { mutableStateOf<Job?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }
    
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

    val themeColor = Color(if (selectedColor == 0xFF1F1F1FL) 0xFFFFFFFF else selectedColor!!)
    val isDefaultColor = selectedColor == 0xFF1F1F1FL

    val scope = rememberCoroutineScope()
    
    // Track keyboard visibility using IME insets
    var previousImeBottom by remember { mutableStateOf(0) }
    val currentImeBottom = with(density) { WindowInsets.ime.getBottom(this) }
    
    // Clear focus when keyboard is dismissed while title is focused
    LaunchedEffect(currentImeBottom, isTitleFocused) {
        if (isTitleFocused) {
            if (currentImeBottom > 0) {
                keyboardAppearedSinceLastFocused = true
            } else if (keyboardAppearedSinceLastFocused && previousImeBottom > 0) {
                // Keyboard was just dismissed - clear focus
                delay(50)
                focusManager.clearFocus()
                keyboardAppearedSinceLastFocused = false
            }
        }
        previousImeBottom = currentImeBottom
    }
    
    // Hide input section when keyboard is dismissed
    LaunchedEffect(currentImeBottom, isInputFocused, showInputSection) {
        if (isInputFocused && showInputSection) {
            if (currentImeBottom > 0) {
                inputKeyboardAppeared = true
            } else if (inputKeyboardAppeared && previousImeBottom > 0) {
                // Keyboard was just dismissed - hide input section
                delay(50)
                showInputSection = false
                inputKeyboardAppeared = false
                newItem = TextFieldValue() // Clear the input
            }
        }
    }
    
    // Auto-focus input field when input section is shown
    LaunchedEffect(showInputSection) {
        if (showInputSection) {
            delay(100) // Small delay to ensure the input field is rendered
            inputFocusRequester.requestFocus()
        }
    }
    
    // Auto-focus list name input when there's no list name
    LaunchedEffect(existing?.id) {
        if (name.isEmpty()) {
            delay(100) // Small delay to ensure the input field is rendered
            titleFocusRequester.requestFocus()
        }
    }
    
    // Handle back button - close keyboard first, then navigate
    BackHandler(enabled = !hasNavigated && navigationJob == null) {
        if (hasNavigated) return@BackHandler
        
        // Cancel any existing navigation job
        navigationJob?.cancel()
        
        hasNavigated = true
        val isKeyboardVisible = currentImeBottom > 0
        if (isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
            navigationJob = scope.launch {
                delay(150) // Wait for keyboard to close
                // hasNavigated flag already prevents multiple calls, so safe to navigate
                onDone()
                navigationJob = null
            }
        } else {
            // Navigate immediately
            onDone()
            navigationJob = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Handle manually
        bottomBar = {
            if (showInputSection) {
                // Input Bar (shown when FAB is clicked)
                Surface(
                    color = Color.Black,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .padding(start = 16.dp, top = 15.dp, end = 16.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BasicTextField(
                            value = newItem,
                            onValueChange = { newItem = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(Color(0xFF2C2C2C), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .focusRequester(inputFocusRequester)
                                .onFocusEvent { focusState ->
                                    isInputFocused = focusState.isFocused
                                },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(themeColor),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val candidate = newItem.text.trim()
                                    if (candidate.isNotEmpty() && items.none { it.equals(candidate, true) }) {
                                        items = items + candidate
                                        newItem = TextFieldValue()
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (newItem.text.isEmpty()) {
                                        Text(
                                            text = "Add new item...",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        // Show Add button only when there's text
                        if (newItem.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val candidate = newItem.text.trim()
                                    if (candidate.isNotEmpty() && items.none { it.equals(candidate, true) }) {
                                        items = items + candidate
                                        newItem = TextFieldValue()
                                        // Keep keyboard open for rapid entry
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = themeColor,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Item",
                                    tint = Color.Black
                                )
                            }
                        }
                        
                        // Show Close button only when field is empty
                        if (newItem.text.isEmpty()) {
                            IconButton(
                                onClick = {
                                    showInputSection = false
                                    newItem = TextFieldValue()
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFF2C2C2C),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .pointerInput(isTitleFocused) {
                    detectTapGestures { _ ->
                        // Clear focus when clicking outside, which will save the name and close keyboard
                        if (isTitleFocused) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = {
                                if (hasNavigated) return@IconButton
                                
                                // Cancel any existing navigation job
                                navigationJob?.cancel()
                                
                                hasNavigated = true
                                // Hide keyboard first, then navigate
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                navigationJob = scope.launch {
                                    delay(150) // Wait for keyboard to close
                                    // hasNavigated flag already prevents multiple calls, so safe to navigate
                                    onDone()
                                    navigationJob = null
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        
                        // List Title Input
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(titleFocusRequester)
                                .onFocusEvent { focusState ->
                                    val wasFocused = isTitleFocused
                                    isTitleFocused = focusState.isFocused
                                    if (!focusState.isFocused && wasFocused) {
                                        keyboardAppearedSinceLastFocused = false
                                    }
                                },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Color.White),
                            decorationBox = { innerTextField ->
                                if (name.isEmpty()) {
                                    Text(
                                        "List Name",
                                        color = Color.Gray.copy(alpha = 0.5f),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = { showColorPicker = true }
                    ) {
                        Icon(
                            Icons.Default.Palette, 
                            contentDescription = "Color Picker",
                            tint = if (isDefaultColor) Color.White else themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Delete button (only show if list exists)
                    if (existing != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete List",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // List Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (items.isEmpty()) {
                    item {
                        EmptyState(themeColor)
                    }
                } else {
                    items(items.size, key = { items[it] }) { index ->
                        ItemRow(
                            item = items[index],
                            themeColor = themeColor,
                            onDelete = { 
                                items = items.filterNot { it == items[index] }
                                existing?.id?.let { listId ->
                                    viewModel.removeItem(listId, items[index])
                                }
                            }
                        )
                    }
                }
            }
        }
            
            // Floating Action Button
            if (!showInputSection) {
                ExtendedFloatingActionButton(
                    onClick = { showInputSection = true },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Item",
                            tint = Color.Black
                        )
                    },
                    text = { 
                        Text(
                            "Add Item",
                            color = Color.Black
                        ) 
                    },
                    containerColor = themeColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 60.dp)
                )
            }
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
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Delete List?",
                    color = Color.White
                ) 
            },
            text = {
                Text(
                    "Are you sure you want to delete \"$name\"? This action cannot be undone.",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            containerColor = Color(0xFF1E1E1E),
            confirmButton = {
                TextButton(
                    onClick = {
                        existing?.id?.let { listId ->
                            showDeleteDialog = false
                            onDone() // Navigate to home screen first
                            // Wait 3 seconds, then delete (using ViewModel scope so it persists after navigation)
                            viewModel.deleteListDelayed(listId)
                        } ?: run {
                            showDeleteDialog = false
                            onDone()
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun EmptyState(themeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = themeColor.copy(alpha = 0.5f)
            )
            Text(
                text = "No items yet",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "Add items using the Add Item button below",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
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
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(COLOR_OPTIONS) { color ->
                        val isSelected = currentColor == color
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { 
                                    onColorSelected(if (isSelected) null else color)
                                    onDismiss()
                                }
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF1E1E1E),
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
                Text("Reset", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
private fun ItemRow(
    item: String,
    themeColor: Color,
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(12.dp),
            onClick = {} // Capture clicks
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(themeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
