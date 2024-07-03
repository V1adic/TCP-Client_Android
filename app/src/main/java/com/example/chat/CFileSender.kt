package com.example.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FileChooserScreen(viewModel: CViewModelChatCompose) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let{ uri ->
                    Program.uriFileSend = uri
                    if(Program.ifOnline)
                    {
                        viewModel.myCoroutineScope.launch(Dispatchers.IO) {
                            Program.commandFileServicesAsync(context)
                        }
                    }
                }
            }
        }
    )

    val buttonColors = ButtonDefaults.buttonColors(
        contentColor = Color.White,
        containerColor = MaterialTheme.colorScheme.primary
    )
    val buttonShape = RoundedCornerShape(20)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.primary,
        focusedTextColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor= MaterialTheme.colorScheme.primary,
        unfocusedLabelColor= MaterialTheme.colorScheme.primary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
    )
    val textStyle = TextStyle(
        color = MaterialTheme.colorScheme.primary
    )


    Column (
        modifier = Modifier
            .fillMaxHeight()
            .padding(5.dp),
        verticalArrangement = Arrangement.Center,
    )
    {
        OutlinedTextField(
            value = viewModel.clientName,
            onValueChange = { value -> viewModel.updateClientName(value) },
            label = { Text("Введите имя клиента") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = textFieldColors,
            isError = viewModel.clientName == "" || viewModel.clientName.contains(" ") || viewModel.clientNameServices == "Is not correct name"
        )

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            colors = buttonColors,
            shape = buttonShape,
            onClick = {
                if(viewModel.clientName != "" && !viewModel.clientName.contains(" "))
                {
                    viewModel.myCoroutineScope.launch(Dispatchers.Main) {
                        Program.ifOnline = false
                        Program.tempNameCL = ""
                        Program.nameCL = ""
                        Program.nameCL = ""
                        Program.viewModel.updateClientNameSend("")
                        if(Program.commandGetIPCLAsync(viewModel.clientName))
                        {
                            viewModel.updateClientNameServices("Correct name")
                        }
                        else
                        {
                            viewModel.updateClientNameServices("Is not correct name")
                        }
                    }
                }
            }) {
            Text(stringResource(R.string.send))
        }
        Text(
            viewModel.clientNameSend,
            modifier = Modifier.fillMaxWidth())
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            launcher.launch(intent)
        }) {
            Text("Выберите файл")
        }
        Text(
            viewModel.clientNameServices,
            modifier = Modifier.fillMaxWidth())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CFileSender(viewModel: CViewModelChatCompose, navController: NavHostController)
{
    Box {
        FileChooserScreen(viewModel)
    }
}