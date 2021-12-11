package com.example.socialgateway

import android.content.Context
import android.os.AsyncTask
import org.json.JSONObject
import java.io.File
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class ServerInterface(context: Context) {
    private val key: String = context.resources.getString(R.string.KEY)
    private val serverUrlPath: String = context.resources.getString(R.string.serverUrlPath)

    private fun getFromServer(route: String, arguments: String = ""): JSONObject {
        val connection = URL("$serverUrlPath$route?key=$key&$arguments").openConnection() as HttpURLConnection
        connection.disconnect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK)
            throw ConnectException("GET response code ${connection.responseCode}")

        return JSONObject(connection.inputStream.reader().readText())
    }

    private fun postToServer(data: ByteArray, route: String, arguments: String = "") {
        AsyncTask.execute {
            val connection =
                (URL("$serverUrlPath$route?key=$key&$arguments").openConnection() as HttpURLConnection)

            connection.apply {
                requestMethod = "POST"
                doOutput = true
                outputStream.write(data)
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw ConnectException("response code $responseCode")
                }
            }

            connection.disconnect()
        }
    }

    fun getPrompt(socialApp: SocialApp?, promptType: String = "normal"): Prompt {
        val language = if (Locale.getDefault().language == "de") "german" else "english"

        val parameterList = mutableListOf("language=$language", "prompt_type=$promptType")

        if (socialApp != null) {
            val encodedAppName = URLEncoder.encode(socialApp.name, "utf-8")
            parameterList.add("app_name=$encodedAppName")
        }


        val responseJson = getFromServer(
            "/prompt",
            parameterList.joinToString(separator = "&")
        )

        return Prompt(
            responseJson.getString("content"),
            responseJson.getBoolean("answerable")
        )
    }

    fun sendTextAnswer(
        appName: String,
        userId: String,
        prompt: String,
        answerText: String
    ) {
        postToServer(
            JSONObject(
                """{
            "user_id": "$userId",
            "app_name": "$appName",
            "prompt": "$prompt",
            "answer_text": "$answerText"
        }"""
            ).toString().toByteArray(), "/answer"
        )
    }

    fun sendAudioAnswer(
        appName: String,
        userId: String,
        prompt: String,
        audio: File
    ) {

        val answerAudioUuid = UUID.randomUUID().toString()
        postToServer(audio.readBytes(), "/audio", "uuid=$answerAudioUuid")
        audio.delete()

        postToServer(
            JSONObject(
                """{
            "user_id": "$userId",
            "app_name": "$appName",
            "prompt": "$prompt",
            "answer_audio_uuid": "$answerAudioUuid"
        }"""
            ).toString().toByteArray(), "/answer"
        )
    }
}