package com.example.socialgateway

import android.os.AsyncTask
import org.json.JSONObject
import java.io.File
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*


class ServerInterface {
    // TODO hide this key
    private val key = "hef3TF^Vg90546bvgFVL>Zzxskfou;aswperwrsf,c/x"

    // TODO can I just close the connection?
    private fun openConnection(route: String, arguments: String = ""): HttpURLConnection {
        // why?
        assert(!route.contains('?'))
        // TODO save url separately
        return URL("https://hpi.de/baudisch/projects/neo4j/api$route?key=$key&$arguments").openConnection() as HttpURLConnection
    }

    private fun postToServer(data: ByteArray, route: String, arguments: String = "") {
        AsyncTask.execute {
            openConnection(route, arguments).apply {
                try {
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(data)
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw ConnectException("response code $responseCode")
                    }
                } catch (exception: ConnectException) {
                    log("could not send answer: ${exception.message.orEmpty()}")
                } finally {
                    disconnect()
                }
            }
        }
    }

    // TODO use socialApp: SocialApp instead of socialAppName: String?
    fun getQuestion(socialAppName: String, language: String, questionType: String): String {
        val encodedAppName = URLEncoder.encode(socialAppName, "utf-8")
        val questionConnection = ServerInterface().openConnection(
            "/question",
            "app_name=$encodedAppName&language=$language&question_type=$questionType"
        )

        questionConnection.disconnect()

        if (questionConnection.responseCode != HttpURLConnection.HTTP_OK)
            throw ConnectException("response code ${questionConnection.responseCode}")

        return questionConnection.inputStream.reader().readText()
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