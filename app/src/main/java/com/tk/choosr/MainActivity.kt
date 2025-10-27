package com.tk.choosr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tk.choosr.navigation.AppNavHost
import com.tk.choosr.viewmodel.ListsViewModel
import com.tk.choosr.ui.theme.ChoosrTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Set status bar and navigation bar to transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            ChoosrTheme {
                SetSystemBarsColor()
                val viewModel: ListsViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    containerColor = Color.Black
                ) { innerPadding ->
                    AppNavHost(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetSystemBarsColor() {
    val window = (androidx.compose.ui.platform.LocalContext.current as? android.app.Activity)?.window
    SideEffect {
        window?.let {
            val controller = WindowInsetsControllerCompat(it, it.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }
}