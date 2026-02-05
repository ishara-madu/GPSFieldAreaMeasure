package com.pixeleye.gpsfieldareameasure.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object SavedList : Screen("saved_list")
    object HowToUse : Screen("how_to_use")
}
