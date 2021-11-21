package com.example.socialgateway

import android.os.AsyncTask
import org.json.JSONObject
import java.io.File
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

const val SERVER_URL_PATH = "https://hpi.de/baudisch/projects/neo4j/api"

class ServerInterface {
    // TODO hide this key
    private val key = "hef3TF^Vg90546bvgFVL>Zzxskfou;aswperwrsf,c/x"

    private fun getFromServer(route: String, arguments: String = ""): JSONObject {
        val connection =
            URL("$SERVER_URL_PATH$route?key=$key&$arguments").openConnection() as HttpURLConnection
        connection.disconnect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK)
            throw ConnectException("GET response code ${connection.responseCode}")

        return JSONObject(connection.inputStream.reader().readText())
    }

    private fun postToServer(data: ByteArray, route: String, arguments: String = "") {
        AsyncTask.execute {
            val connection =
                (URL("$SERVER_URL_PATH$route?key=$key&$arguments").openConnection() as HttpURLConnection)

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

    fun getPrompt(socialApp: SocialApp, promptType: String): Prompt {
        val encodedAppName = URLEncoder.encode(socialApp.name, "utf-8")
        val language = if (Locale.getDefault().language == "de") "german" else "english"

        val responseJson = ServerInterface().getFromServer(
            "/prompt",
            "app_name=$encodedAppName&language=$language&prompt_type=$promptType"
        )

        return Prompt(
            responseJson.getString("content"),
            responseJson.getBoolean("answerable")
        )
    }

    fun sendAnswer(
        appName: String,
        userId: String,
        prompt: String,
        answerText: String,
        audio: File
    ) {
        var answerAudioUuid = "null"

        audio.let {
            if (it.exists()) {
                answerAudioUuid = UUID.randomUUID().toString()
                postToServer(it.readBytes(), "/audio", "uuid=$answerAudioUuid")
                it.delete()
            }
        }

        postToServer(
            JSONObject(
                """{
            "user_id": "$userId",
            "app_name": "$appName",
            "prompt": "$prompt",
            "answer_text": "$answerText",
            "answer_audio_uuid": "$answerAudioUuid"
        }"""
            ).toString().toByteArray(), "/answer"
        )
    }
}