package com.tk.choosr.ui.shuffle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.viewmodel.ListsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ShuffleBottomDrawer(
    list: ChoiceList?,
    viewModel: ListsViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShowingResult by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf<Int?>(null) }
    var isChoosing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Start the choosing process when drawer opens
    LaunchedEffect(list?.id) {
        if (list != null && list.items.isNotEmpty()) {
            isChoosing = true
            delay(2000) // Wait 2 seconds
            currentIndex = viewModel.nextItemIndex(list.id)
            isChoosing = false
            isShowingResult = true
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(bottom = 32.dp), // Reduced padding since FAB is hidden
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = list?.name ?: "Shuffle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = when {
                        isChoosing -> "Choosing..."
                        isShowingResult -> currentIndex
                        else -> null
                    },
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) with fadeOut(animationSpec = tween(200))
                    },
                    label = "shuffle_content"
                ) { content ->
                    when (content) {
                        "Choosing..." -> {
                            Text(
                                text = "Choosing...",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is Int -> {
                            Text(
                                text = list?.items?.getOrNull(content) ?: "No items",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Text(
                                text = "No items available",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action button - centered (only show when not choosing)
            if (!isChoosing) {
                Button(
                    onClick = {
                        isChoosing = true
                        isShowingResult = false
                        // Restart the choosing process
                        coroutineScope.launch {
                            delay(2000)
                            currentIndex = viewModel.nextItemIndex(list?.id ?: "")
                            isChoosing = false
                            isShowingResult = true
                        }
                    },
                    enabled = list?.items?.isNotEmpty() == true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Shuffle Again")
                }
            }
        }
    }
}
