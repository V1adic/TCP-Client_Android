package com.example.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatBody(
    viewModel : CViewModelChatCompose,
    navController: NavHostController,
    modifier: Modifier = Modifier) {


    val buttonColors = ButtonDefaults.buttonColors(
        contentColor = Color.White,
        containerColor = MaterialTheme.colorScheme.primary
    )
    val buttonShape = RoundedCornerShape(20)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.primary,
        focusedTextColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
    )
    val scroll = rememberScrollState(Int.MAX_VALUE)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(5.dp),
        verticalArrangement = Arrangement.Center,
    )
    {
        Column(Modifier.verticalScroll(scroll)) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(5.dp),
                verticalArrangement = Arrangement.Center,
            )
            {
                items(viewModel.chatLabel.split("\n")) {
                    if (it != "" && it != " ") {

                        Button(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = buttonColors,
                            shape = buttonShape,
                            onClick = {
                                viewModel.updateClientName(it.split(":", limit = 2)[0])
                                Program.tempNameCL = it.split(":", limit = 2)[0]
                                navController.navigate("File")
                            }
                        )
                        {
                            Text(it)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = viewModel.myMessage,
            onValueChange = { value -> viewModel.updateMyMessage(value) },
            label =
            {
                Text(stringResource(R.string.enter_massage))
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = textFieldColors,
            isError = viewModel.myMessage == "" || viewModel.myMessage == " "
        )

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            colors = buttonColors,
            shape = buttonShape,
            onClick = {
                if (viewModel.myMessage != "" && viewModel.myMessage != " ") {
                    viewModel.updateChatLabel("${viewModel.chatLabel}\n${Program.MyName}: ${viewModel.myMessage}")
                    viewModel.myCoroutineScope.launch(Dispatchers.IO) {
                        Program.myMessage = viewModel.myMessage
                        Program.commandMessageCLAsync()
                    }
                    Thread.sleep(400)
                    viewModel.updateMyMessage("")
                }

            }) {
            Text(stringResource(R.string.send))
        }

    }

}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CChat(viewModel: CViewModelChatCompose, navController: NavHostController)
{
    Box {
        ChatBody(viewModel, navController)
    }
}