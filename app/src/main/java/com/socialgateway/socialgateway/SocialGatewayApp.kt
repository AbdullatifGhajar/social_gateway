package com.socialgateway.socialgateway

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.text.format.DateFormat
import com.socialgateway.socialgateway.data.model.SocialApp
import java.util.*

fun today(): String {
    return DateFormat.format("dd.MM.yyyy", Date()) as String
}


class SocialGatewayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = this.getSharedPreferences("com.socialgateway,socialgateway.prompt", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var instance: SocialGatewayApp
        lateinit var preferences: SharedPreferences
        val resources: Resources
            get() = instance.resources

        // check if the user already a prompt for this app today
        fun shouldReceivePrompt(socialApp: SocialApp): Boolean {
            return (preferences.getString("lastPrompt:${socialApp.name}", "") != today())
        }

        // log this for shouldReceivePrompt later
        fun logPrompt(socialApp: SocialApp) {
            preferences.edit().apply {
                putString("lastPrompt:${socialApp.name}", today())
                putString("lastPromptDate", today())
                apply()
            }
        }
    }
}