package com.example.chat
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset


class Program {
    companion object {

        var IPSERV = "192.168.1.253"
        var MyName = ""
        var Correct_Name = false
        var ifOnline = false
        var tempNameCL = ""
        var nameCL = ""
        var myMessage = ""
        var uriFileSend: Uri = Uri.EMPTY
        lateinit var viewModel: CViewModelChatCompose
        var connection: Connection? = null


        private fun deleteCache(context: Context) {
            try {
                val dir = context.cacheDir
                deleteDir(dir)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                if (children != null) {
                    for (i in children.indices) {
                        val success = deleteDir(File(dir, children[i]))
                        if (!success) {
                            return false
                        }
                    }
                }
                return dir.delete()
            } else if (dir != null && dir.isFile) {
                return dir.delete()
            } else {
                return false
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun commandUpdateCLAsync() {
            println("WORK /updateCL")
            val data = "/updateCL ".toByteArray(Charset.forName("UTF-8"))
            connection!!.sendMessageAsync(data)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun commandNameAsync(mes: String): Boolean {
            println("WORK /name")
            val data = "/name $mes".toByteArray(Charset.forName("UTF-8"))
            connection!!.sendMessageAsync(data)
            // отправляем данные
            Thread.sleep(400)
            if (Correct_Name) {
                MyName = mes
            }
            return Correct_Name
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun commandMessageCLAsync() {
            println("WORK /messageCL")
            val data = "/messageCL $MyName: $myMessage".toByteArray(Charset.forName("UTF-8"))
            // отправляем данные
            connection!!.sendMessageAsync(data)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun commandGetIPCLAsync(mes: String): Boolean {
            println("WORK /getIPCL")
            val data = "/getIPCL $mes".toByteArray(Charset.forName("UTF-8"))
            // отправляем данные
            tempNameCL = mes
            connection!!.sendMessageAsync(data)
            Thread.sleep(400)

            return ifOnline
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun commandFileServicesAsync(context: Context) {
            println("WORK /fileServices")
            viewModel.updateClientNameSend("Файл отправляется, пожалуйста подождите")
            viewModel.updateClientNameServices("")
            viewModel.updateClientName("")
            var file: ByteArray = byteArrayOf()
            context.contentResolver.openInputStream(uriFileSend)?.use { inputStream ->
                file = IOUtils.toByteArray(inputStream)
            }

            var filename = getRealPath(context, uriFileSend)?.split("/")?.last()
            var otg = ""
            if (filename == null) {
                if (ContentResolver.SCHEME_CONTENT == uriFileSend.scheme) {
                    try {
                        context.contentResolver.query(uriFileSend, null, null, null, null)
                            .use { returnCursor ->
                                if (returnCursor != null && returnCursor.moveToFirst()) {
                                    val mimeIndex =
                                        returnCursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                                    otg = returnCursor.getString(mimeIndex).split("/").last()
                                }
                            }
                    } catch (ignore: java.lang.Exception) {
                    }
                }
                filename = "With_Android.$otg"
            }

            println(filename)
            uriFileSend = Uri.EMPTY

            val safeFilename = filename.replace(' ', '_')
            val fileServices =
                "/fileServices $nameCL $safeFilename <END>".toByteArray(Charset.forName("UTF-8"))
            val rv = ByteArray(fileServices.size + file.size)
            System.arraycopy(fileServices, 0, rv, 0, fileServices.size)
            System.arraycopy(file, 0, rv, fileServices.size, file.size)
            deleteCache(context)
            connection!!.sendMessageAsync(rv)
            ifOnline = false
            nameCL = ""
            viewModel.updateClientNameSend("Файл был отправлен")
        }

        private fun getRealPath(context: Context, uri: Uri): String? { // is amazing

            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    var cursor: Cursor? = null
                    try {
                        cursor = context.contentResolver.query(
                            uri,
                            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        cursor!!.moveToNext()
                        val fileName = cursor.getString(0)
                        val path = Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                        if (!TextUtils.isEmpty(path)) {
                            return path
                        }
                    } finally {
                        cursor?.close()
                    }
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads"),
                        java.lang.Long.valueOf(id)
                    )

                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                return uri.path
            }

            return null
        }

        private fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {

            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor =
                    context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }
}

/*

    Комманды клиента
    commandUpdateCLAsync -> "/updateCL " // запрос на синхронизацию  чата
    commandNameAsync -> "/name $mes" // запрос на установлении имени у данного IP
    commandMessageCLAsync -> "/messageCL $MyName: $myMessage" // Отправка сообщения из общего чата
    commandGetIPCLAsync -> "/getIPCL $name" // запросить проверку онлайна у данного имени
    commandFileServicesAsync -> "/fileServices $nameCL $safeFilename <END>{File is byteArray}" // отправка файла по имени

    Комманды сервера
    "/messageSERV $mes" //сообщение в общий чат
    "/corr " // корректость отправленного имени
    "/updateSERV {File is byteArray}" // отправка файла с чатом
    "/yourName $name" // отправляет при коннекте имя пользователя
    "/corrOnline " // человек, которому хотим отправить файл может принять его
    "/fileServices $safeFilename <END>{File is byteArray}" // отправка файла

*/