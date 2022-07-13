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
        preferences = this.getSharedPreferences(
            "com.socialgateway,socialgateway.prompt",
            Context.MODE_PRIVATE
        )
    }

    companion object {
        lateinit var instance: SocialGatewayApp
        lateinit var preferences: SharedPreferences
        val resources: Resources
            get() = instance.resources

        // check if the user already got a prompt for this app today
        fun shouldReceivePrompt(socialApp: SocialApp): Boolean {
            return (preferences.getString("lastPrompt:${socialApp.name}", "") != today())
        }

        // check if the user already got a reflection prompt today
        fun shouldReceiveReflectionPrompt(): Boolean {
            return (preferences.getString("lastReflectionPrompt", "") != today())
        }

        // check if the user already got a reflection prompt today
        fun shouldReceiveEMAPrompt(): Boolean {
            return (preferences.getString("lastEMAPrompt", "") != today())
        }

        // check if this is the first time the app was opened today
        fun isFirstTimeToday(): Boolean {
            return (preferences.getString("lastPrompt", "") != today())
        }

        // log this for shouldReceivePrompt later
        fun logPrompt(socialApp: SocialApp) {
            preferences.edit().apply {
                putString("lastPrompt:${socialApp.name}", today())
                putString("lastPrompt", today())
                apply()
            }
        }

        fun logReflectionPrompt() {
            preferences.edit().apply {
                putString("lastReflectionPrompt", today())
                apply()
            }
        }

        fun logEMAPrompt() {
            preferences.edit().apply {
                putString("lastEMAPrompt", today())
                apply()
            }
        }
    }
}