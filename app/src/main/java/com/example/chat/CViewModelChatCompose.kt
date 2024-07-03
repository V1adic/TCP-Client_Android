package com.example.chat

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Socket

@RequiresApi(Build.VERSION_CODES.O)
class CViewModelChatCompose : ViewModel(){

    val myCoroutineScope = CoroutineScope(Dispatchers.IO)

    var myName by mutableStateOf("")
        private set
    var myMessage by mutableStateOf("")
        private set
    var clientName by mutableStateOf("")
        private set
    var chatLabel by mutableStateOf("")
        private set
    var clientNameServices by mutableStateOf("")
        private set
    var clientNameSend by mutableStateOf("")
        private set

    fun updateMyName(input: String) {
        myName = input
    }
    fun updateMyMessage(input: String) {
        myMessage = input
    }
    fun updateClientName(input: String) {
        clientName = input
    }
    fun updateClientNameServices(input: String) {
        clientNameSend = input
    }
    fun updateClientNameSend(input: String) {
        clientNameServices = input
    }
    fun updateChatLabel(input: String) {
        chatLabel = input
    }
}