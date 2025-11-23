package com.tk.choosr.ui.shuffle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.viewmodel.ListsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShuffleDialog(
    list: ChoiceList?,
    viewModel: ListsViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShowingResult by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf<Int?>(null) }
    var isChoosing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Start the choosing process when dialog opens
    LaunchedEffect(list?.id) {
        if (list != null && list.items.isNotEmpty()) {
            isChoosing = true
            delay(2000) // Wait 2 seconds
            currentIndex = viewModel.nextItemIndex(list.id)
            isChoosing = false
            isShowingResult = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        // Dark overlay background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            // Gradient border: dull when choosing, bright gradient when result appears
            val targetAlpha = if (isChoosing) 0.4f else 1f
            val animatedAlpha by animateFloatAsState(
                targetValue = targetAlpha,
                animationSpec = tween(durationMillis = 300),
                label = "border_alpha"
            )
            val colorScheme = MaterialTheme.colorScheme
            val gradientBrush = Brush.linearGradient(
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f),
                colors = listOf(
                    colorScheme.primary.copy(alpha = animatedAlpha),
                    colorScheme.secondary.copy(alpha = animatedAlpha * 0.8f),
                    colorScheme.tertiary.copy(alpha = animatedAlpha * 0.9f),
                    colorScheme.primary.copy(alpha = animatedAlpha * 0.7f),
                    colorScheme.secondary.copy(alpha = animatedAlpha)
                )
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp)
                    .background(
                        brush = gradientBrush,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(2.dp)
            ) {
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFF121212)
                    ),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }
                            is Int -> {
                                Text(
                                    text = list?.items?.getOrNull(content) ?: "No items",
                                    style = MaterialTheme.typography.displaySmall,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = "No items available",
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action button - centered (only show when not choosing)
                if (!isChoosing) {
                    IconButton(
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
            }
        }
    }
}

