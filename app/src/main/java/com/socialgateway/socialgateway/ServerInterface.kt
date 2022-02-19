package com.socialgateway.socialgateway

import com.socialgateway.socialgateway.data.model.LoggedInUser
import com.socialgateway.socialgateway.data.model.Prompt
import com.socialgateway.socialgateway.data.model.PromptType
import com.socialgateway.socialgateway.data.model.SocialApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import socialgateway.socialgateway.R
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class ServerException(message: String) : Exception(message)

class ServerInterface {
    companion object {
        private val key = SocialGatewayApp.resources.getString(R.string.KEY)
        private val serverUrlPath = SocialGatewayApp.resources.getString(R.string.serverUrlPath)

        private fun getFromServer(route: String, arguments: String = ""): JSONObject {
            var responseBody = ""
            runBlocking {
                async(Dispatchers.IO) {
                    val connection =
                        URL("$serverUrlPath$route?key=$key&$arguments").openConnection() as HttpURLConnection
                    connection.disconnect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK)
                        throw ServerException("GET response code ${connection.responseCode}")
                    responseBody = connection.inputStream.reader().readText()

                }.join()
            }
            return JSONObject(responseBody)
        }

        private fun postToServer(
            route: String,
            arguments: String = "",
            data: ByteArray? = null
        ) {
            Thread {
                val connection =
                    (URL("$serverUrlPath$route?key=$key&$arguments").openConnection() as HttpURLConnection)

                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    if (data != null)
                        outputStream.write(data)
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw ServerException("POST response code $responseCode")
                    }
                }

                connection.disconnect()
            }.start()
        }

        fun getPrompt(
            socialApp: SocialApp?,
            promptType: PromptType = PromptType.NORMAL
        ): Prompt {
            val language = if (Locale.getDefault().language == "de") "german" else "english"

            val parameterList =
                mutableListOf("language=$language", "prompt_type=${promptType.value}")

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
                "/answer",
                "",
                JSONObject(
                    mapOf(
                        "user_id" to userId,
                        "app_name" to appName,
                        "prompt" to prompt,
                        "answer_text" to answerText
                    )
                ).toString().toByteArray()
            )
        }

        fun sendAudioAnswer(
            appName: String,
            userId: String,
            prompt: String,
            audio: File
        ) {

            val answerAudioUuid = UUID.randomUUID().toString()
            postToServer("/audio", "uuid=$answerAudioUuid", audio.readBytes())
            audio.delete()

            postToServer(
                "/answer",
                "",
                JSONObject(
                    mapOf(
                        "user_id" to userId,
                        "app_name" to appName,
                        "prompt" to prompt,
                        "answer_audio_uuid" to answerAudioUuid
                    )
                ).toString().toByteArray()
            )
        }

        fun getAuthenticatedUser(email: String, password: String): LoggedInUser {
            val responseJson = getFromServer(
                "/users",
                "email=$email&password=$password"
            )

            return LoggedInUser(
                responseJson.getString("id"),
                responseJson.getString("email"),
                responseJson.getString("displayName")
            )
        }
    }
}