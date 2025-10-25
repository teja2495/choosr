package com.tk.choosr.ui.shuffle

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tk.choosr.viewmodel.ListsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ShuffleResultScreen(
    viewModel: ListsViewModel,
    listId: String,
    onDone: () -> Unit,
) {
    val list = viewModel.lists.value.firstOrNull { it.id == listId }
    val currentIndex = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(listId) {
        currentIndex.value = viewModel.nextItemIndex(listId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(list?.name ?: "Shuffle") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentIndex.value,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) with fadeOut(animationSpec = tween(200))
                    }, label = "shuffle"
                ) { idx ->
                    Text(
                        text = idx?.let { list?.items?.getOrNull(it) } ?: "No items",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { currentIndex.value = viewModel.nextItemIndex(listId) }, enabled = (list?.items?.isNotEmpty() == true)) {
                    Text("Shuffle Again")
                }
                Button(onClick = onDone) { Text("Done") }
            }
        }
    }
}


