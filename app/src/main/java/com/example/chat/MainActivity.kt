package com.example.chat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Program.viewModel = viewModel()
            class NetworkManager {
                fun getInstance() {
                    if(Program.connection == null) {

                        Program.viewModel.myCoroutineScope.launch(Dispatchers.IO) {
                            println("WORK /connection")
                            Program.connection =
                                Connection(
                                    Socket(Program.IPSERV, 11000),
                                    Program.viewModel
                                )
                        }
                    }
                    Thread.sleep(400)
                    Program.viewModel.myCoroutineScope.launch(Dispatchers.Main) {
                        Program.commandUpdateCLAsync() // Обновить весь чат сразу
                    }
                }
            }
            NetworkManager().getInstance()
            Thread.sleep(400)
            CScaffold(Program.viewModel)
        }
    }
}