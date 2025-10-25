package com.tk.choosr.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.viewmodel.ListsViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListScreen(
    viewModel: ListsViewModel,
    listId: String?,
    onDone: () -> Unit,
) {
    val existing = viewModel.lists.value.firstOrNull { it.id == listId }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var newItem by remember { mutableStateOf(TextFieldValue()) }
    var items by remember { mutableStateOf(existing?.items ?: emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "New List" else "Edit List") },
                actions = {
                    TextButton(onClick = {
                        val trimmedName = name.trim()
                        if (trimmedName.isEmpty() || items.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Enter name and at least one item") }
                            return@TextButton
                        }
                        val list = (existing?.copy(name = trimmedName, items = items))
                            ?: ChoiceList(name = trimmedName, items = items)
                        if (existing == null) viewModel.addList(list) else viewModel.updateList(list)
                        onDone()
                    }) { Text("Save") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newItem,
                        onValueChange = { newItem = it },
                        label = { Text("Add item") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(onClick = {
                        val candidate = newItem.text.trim()
                        if (candidate.isNotEmpty() && items.none { it.equals(candidate, true) }) {
                            items = items + candidate
                            newItem = TextFieldValue()
                        }
                    }) { Text("Add") }
                }
            }
            items(items, key = { it }) { item ->
                ListItem(
                    headlineContent = { Text(item) },
                    trailingContent = {
                        IconButton(onClick = { items = items.filterNot { it == item } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete item")
                        }
                    }
                )
                Divider()
            }
        }
    }
}


