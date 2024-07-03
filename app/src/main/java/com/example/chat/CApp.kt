package com.example.chat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CScaffold(viewModelChat: CViewModelChatCompose, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = mutableListOf(
        "Chat",
        "File"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(currentRoute ?: "Проблема")
                }
            )
        },
        bottomBar = {
            if (Program.Correct_Name) {
                CBottomNavigationBar(navController = navController, items)
            }
        },
    )
    { padding ->
        var start = "Name"
        if (Program.Correct_Name) {
            start = "Chat"
        }
        NavHost(
            navController = navController,
            // set the start destination as home
            startDestination = start,

            // Set the padding provided by scaffold
            modifier = Modifier.padding(paddingValues = padding),

            builder = {

                composable("Name") {
                    CName(viewModelChat, navController)
                }
                composable("Chat") {
                    CChat(viewModelChat, navController)
                }
                composable("File") {
                    CFileSender(viewModelChat, navController)
                }
            })
    }
}