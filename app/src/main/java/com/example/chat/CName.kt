package com.example.chat

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NameBody(
    viewModel : CViewModelChatCompose, navController: NavHostController,
    modifier: Modifier = Modifier) {

    val buttonColors = ButtonDefaults.buttonColors(
        contentColor = Color.White,
        containerColor = MaterialTheme.colorScheme.primary
    )
    val context = LocalContext.current
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

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(5.dp),
        verticalArrangement = Arrangement.Center,
    )

    {
        OutlinedTextField(
            value = viewModel.myName,
            onValueChange = { value -> viewModel.updateMyName(value) },
            label = { Text("Введите своё имя") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = textFieldColors,
            isError = viewModel.myName == "" || viewModel.myName.contains(" ")
        )

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            colors = buttonColors,
            shape = buttonShape,
            onClick = {
                if(viewModel.myName != "" && !viewModel.myName.contains(" "))
                {
                    viewModel.myCoroutineScope.launch(Dispatchers.Main) {
                        if(Program.commandNameAsync(viewModel.myName))
                        {
                            navController.navigate("Chat")
                        }
                        viewModel.updateMyName("")
                    }
                }
            }) {
            Text(stringResource(R.string.send))
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CName(viewModel: CViewModelChatCompose, navController: NavHostController)
{
    Box {
        NameBody(viewModel, navController)
    }
}