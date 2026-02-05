package com.pixeleye.gpsfieldareameasure.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.pixeleye.gpsfieldareameasure.ui.components.AppDrawerContent
import com.pixeleye.gpsfieldareameasure.ui.navigation.Screen
import com.pixeleye.gpsfieldareameasure.ui.screens.HowToUseScreen
import com.pixeleye.gpsfieldareameasure.ui.screens.MainScreen
import com.pixeleye.gpsfieldareameasure.ui.screens.SavedListScreen
import com.pixeleye.gpsfieldareameasure.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val safeBack = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.currentValue != DrawerValue.Closed, // Allow gestures if opening/open
        drawerContent = {
            AppDrawerContent(
                onItemSelected = { route ->
                    scope.launch { drawerState.close() }
                    val context = navController.context
                    val packageName = context.packageName
                    
                    when (route) {
                        "saved_list" -> navController.navigate(Screen.SavedList.route)
                        "how_to_use" -> navController.navigate(Screen.HowToUse.route)
                        "rate", "update" -> {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("market://details?id=$packageName")
                                setPackage("com.android.vending")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, 
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            }
                        }
                        "share" -> {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, 
                                    "Check out GPS Field Area Measure! Measure areas and perimeters easily: https://play.google.com/store/apps/details?id=$packageName")
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                        "privacy" -> {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, 
                                android.net.Uri.parse("https://pixeleye.com/privacy-policy")) // Example URL
                            context.startActivity(intent)
                        }
                    }
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route
            ) {
                composable(Screen.Main.route) {
                    MainScreen(
                        viewModel = viewModel,
                        onOpenDrawer = {
                            if (drawerState.isClosed) {
                                scope.launch { drawerState.open() }
                            }
                        },
                        onNavigateToInfo = { navController.navigate(Screen.HowToUse.route) }
                    )
                }
                composable(Screen.SavedList.route) {
                    SavedListScreen(
                        viewModel = viewModel,
                        onBack = safeBack
                    )
                }
                composable(Screen.HowToUse.route) {
                    HowToUseScreen(
                        onBack = safeBack
                    )
                }
            }
        }
    }
}
