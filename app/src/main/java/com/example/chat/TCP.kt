package com.example.chat

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

@RequiresApi(Build.VERSION_CODES.O)
class Connection(private val client: Socket, private val viewModel : CViewModelChatCompose) : AutoCloseable {
    private val stream: InputStream = client.getInputStream()
    private val outputStream: OutputStream = client.getOutputStream()
    private val channel = Channel<ByteArray>(Channel.UNLIMITED)
    private val readingJob: Job
    private val writingJob: Job
    private var disposed = false

    init {
        readingJob = GlobalScope.launch { runReadingLoop() }
        writingJob = GlobalScope.launch { runWritingLoop() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runReadingLoop() {
        try {
            val headerBuffer = ByteArray(4)
            while (true) {
                val bytesReceived = stream.read(headerBuffer)
                if (bytesReceived != 4) break
                val length = ByteBuffer.wrap(headerBuffer).order(ByteOrder.LITTLE_ENDIAN).int
                val buffer = ByteArray(length)
                var count = 0
                while (count < length) {
                    val received = stream.read(buffer, count, length - count)
                    count += received
                }
                val message = String(buffer, StandardCharsets.UTF_8)

                val services =
                    if (" " in message) message.split(" ", limit = 2) else listOf(message)
                when (services[0]) {
                    "/messageSERV" -> {
                        println("WORK /messageSERV")
                        viewModel.updateChatLabel("${viewModel.chatLabel}\n${services[1]}")
                    }

                    "/corr" -> {
                        println("WORK /corr")
                        Program.Correct_Name = true
                    }

                    "/updateSERV" -> {
                        println("WORK /updateSERV")
                        viewModel.updateChatLabel(services[1])
                    }

                    "/yourName" -> {
                        println("WORK /yourName")
                        Program.MyName = services[1]
                        Program.Correct_Name = true
                        println(Program.MyName)
                    }

                    "/corrOnline" -> {
                        println("WORK /corrOnline")
                        Program.ifOnline = true
                        Program.nameCL = Program.tempNameCL
                        Program.tempNameCL = ""
                    }

                    "/fileServices" -> { // TODO path
                        println("WORK /fileServices")
                        val fileServices = services[1].split(" ", limit = 2)
                        var indexOf = 0
                        val result = mutableListOf<Byte>()
                        var flag = false

                        for (j in buffer.indices) {
                            if (flag) break
                            result.add(buffer[j])
                            if (String(result.toByteArray()).contains("<END>")) {
                                indexOf = j + 1
                                flag = true
                            }
                        }

                        val fileName = fileServices[0]
                        val temp = fileName.lastIndexOf('.')
                        val file = listOf(fileName.substring(0, temp), fileName.substring(temp))
                        if(!Paths.get("/storage/emulated/0/download/Chat_project").exists()) {

                            val dirPath: Path =
                                Paths.get("/storage/emulated/0/download/Chat_project")
                            try {
                                Files.createDirectories(dirPath)

                            } catch (e: IOException) {
                                e.printStackTrace();
                            }
                        }
                        var i = 0
                        while (Paths.get("/storage/emulated/0/download/Chat_project/${file[0]} ($i)${file[1]}")
                                .exists()
                        ) {
                            i++
                        }

                        Files.write(
                            Paths.get("/storage/emulated/0/download/Chat_project/${file[0]} ($i)${file[1]}"),
                            buffer.slice(indexOf until buffer.size).toByteArray()
                        )
                    }
                }
            }
            println("Server closed the connection.")
            stream.close()
        } catch (e: IOException) {
            println("com.example.chat.Connection closed.")
        } catch (e: Exception) {
            println("${e.javaClass.simpleName}: ${e.message}")
        }
    }

    suspend fun sendMessageAsync(message: ByteArray) {
        channel.send(message)
    }

    private suspend fun runWritingLoop() {
        val header = ByteArray(4)
        channel.consumeAsFlow().collect { message ->
            ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).putInt(message.size)

            withContext(Dispatchers.IO) {
                outputStream.write(header)
                outputStream.write(message)
            }
        }
    }

    override fun close() {
        if (disposed) throw IllegalStateException("Object already disposed")
        disposed = true
        if (client.isConnected) {
            channel.close()
            stream.close()
            runBlocking {
                readingJob.join()
                writingJob.join()
            }
        }
        client.close()
    }
}
