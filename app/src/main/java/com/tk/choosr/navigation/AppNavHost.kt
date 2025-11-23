package com.tk.choosr.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tk.choosr.ui.edit.EditListScreen
import com.tk.choosr.ui.home.HomeScreen
import com.tk.choosr.ui.settings.SettingsScreen
import com.tk.choosr.viewmodel.ListsViewModel

@Composable
fun AppNavHost(
    viewModel: ListsViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Home, modifier = modifier) {
        composable(Routes.Home) {
            HomeScreen(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onCreateList = { navController.navigate("${Routes.Edit}") },
                onEditList = { id -> navController.navigate("${Routes.Edit}/$id") },
                onShuffle = { id -> /* No longer needed - handled by drawer */ },
                onSettings = { navController.navigate(Routes.Settings) }
            )
        }

        // Create new list (no args)
        composable(route = Routes.Edit) {
            EditListScreen(
                viewModel = viewModel,
                listId = null,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.Edit}/{${ARG_LIST_ID}}",
            arguments = listOf(navArgument(ARG_LIST_ID) { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString(ARG_LIST_ID)
            EditListScreen(
                viewModel = viewModel,
                listId = listId,
                onDone = { navController.popBackStack() }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onBack = { navController.popBackStack() }
            )
        }

    }
}


