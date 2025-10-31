package com.example.createyourmeme.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.createyourmeme.data.model.MemeItem
import com.example.createyourmeme.ui.screens.EditorScreen
import com.example.createyourmeme.ui.screens.MemeListScreen

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            MemeListScreen(onMemeSelected = { meme ->
                navController.currentBackStackEntry?.savedStateHandle?.set("meme", meme)
                navController.navigate("editor")

            })
        }
        composable("editor") {
            val meme = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<MemeItem>("meme")

            if (meme != null) {
                EditorScreen(meme = meme, onBack = { navController.popBackStack() })
            } else {
                // If no meme passed, just pop
                navController.popBackStack()
            }
        }
    }
}
