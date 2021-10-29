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

    // TODO consider using JSON
    private fun getFromServer(route: String, arguments: String = ""): String {
        val connection =
            URL("$SERVER_URL_PATH$route?key=$key&$arguments").openConnection() as HttpURLConnection
        connection.disconnect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK)
            throw ConnectException("GET response code ${connection.responseCode}")

        return connection.inputStream.reader().readText()
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

    // TODO use socialApp: SocialApp instead of socialAppName: String?
    fun getQuestion(socialAppName: String, questionType: String): String {
        val encodedAppName = URLEncoder.encode(socialAppName, "utf-8")
        val language = if (Locale.getDefault().language == "de") "german" else "english"

        return ServerInterface().getFromServer(
            "/question",
            "app_name=$encodedAppName&language=$language&question_type=$questionType"
        )
    }

    fun sendAnswer(
        appName: String,
        userId: String,
        question: String,
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
            "question": "$question",
            "answer_text": "$answerText",
            "answer_audio_uuid": "$answerAudioUuid"
        }"""
            ).toString().toByteArray(), "/answer"
        )
    }
}