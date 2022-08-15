package com.socialgateway.socialgateway.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.socialgateway.socialgateway.ServerException
import com.socialgateway.socialgateway.ServerInterface
import com.socialgateway.socialgateway.SocialGatewayApp
import com.socialgateway.socialgateway.data.model.Prompt
import com.socialgateway.socialgateway.data.model.PromptType
import com.socialgateway.socialgateway.data.model.SocialApp
import com.socialgateway.socialgateway.data.model.SocialApps
import com.socialgateway.socialgateway.notifications.EMANotification
import com.socialgateway.socialgateway.notifications.Notifier
import com.socialgateway.socialgateway.notifications.ReflectionNotification
import com.socialgateway.socialgateway.notifications.scheduleNotification
import com.socialgateway.socialgateway.ui.login.LoginActivity
import socialgateway.socialgateway.R
import java.net.UnknownHostException


fun log(message: String) {
    Log.d("SocialGateway", message)
}

enum class IntentCategory { AskQuestion, Reflection, EMA, OpenApp, LoginSucceeded }

class AppGridActivity : AppCompatActivity() {
    companion object {
        lateinit var userId: String
    }

    private lateinit var preferences: SharedPreferences

    private fun requestPrompt(
        socialApp: SocialApp,
        promptType: PromptType = PromptType.NORMAL
    ): Prompt? {
        try {
            return ServerInterface.getPrompt(socialApp, promptType)
        } catch (exception: Exception) {
            val errorMessage = resources.getString(
                when (exception) {
                    is ServerException -> R.string.server_unreachable
                    is UnknownHostException -> R.string.no_internet_connection_available
                    else -> R.string.unknown_error
                }
            )
            Toast.makeText(
                this,
                errorMessage,
                Toast.LENGTH_SHORT
            ).show()
            log("could not request prompt: ${exception.message.orEmpty()}")
        }
        return null
    }

    private fun startApp(socialApp: SocialApp) {
        startActivity(packageManager.getLaunchIntentForPackage(socialApp.packageName))
    }

    // when a user selects an app
    private fun chooseApp(socialApp: SocialApp) {
        if (!SocialGatewayApp.shouldReceivePrompt(socialApp)) {
            startApp(socialApp)
            return
        }

        val prompt = when (SocialGatewayApp.isFirstTimeToday()) {
            true -> Prompt(getString(R.string.first_prompt_of_the_day), true)
            else -> requestPrompt(socialApp)
        }

        if (prompt == null) {
            startApp(socialApp)
        } else {
            AnswerDialog(this, socialApp, prompt,
                onSubmit = { startApp(socialApp) })
        }
        SocialGatewayApp.logPrompt(socialApp)
    }

    private fun authenticateUser() {
        preferences =
            getSharedPreferences("com.socialgateway,socialgateway.login", Context.MODE_PRIVATE)
        userId = preferences.getString("userId", "").toString()

        if (userId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun configureNotifications() {
        Notifier.createNotificationChannel(this)

        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (listeners == null || !listeners.contains(packageName)) {
            Toast.makeText(
                this,
                R.string.grant_access,
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            finish()
        }
    }

    private fun renderAppGrid() {
        setContentView(R.layout.social_apps_grid)
        findViewById<GridView>(R.id.social_apps_grid).adapter =
            SocialAppAdapter(this) { _, socialApp ->
                chooseApp(socialApp)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureNotifications()
        authenticateUser()
        renderAppGrid()

        intent?.extras?.let { intent ->
            when (intent.getSerializable("intentCategory") as? IntentCategory) {
                IntentCategory.AskQuestion -> {
                    chooseApp(
                        SocialApps.first { it.name == intent.getString("socialAppName") }
                    )
                }
                IntentCategory.Reflection -> {
                    val prompt = Prompt(intent.getString("question").orEmpty(), true)
                    AnswerDialog(this, null, prompt,
                        onSubmit = {
                            SocialGatewayApp.logReflectionPrompt()
                        })
                }
                IntentCategory.EMA -> {
                    EMADialog(this, onSubmit = {
                        SocialGatewayApp.logEMAPrompt()
                    })
                }
                IntentCategory.OpenApp -> {
                    chooseApp(
                        SocialApps.first { it.name == intent.getString("socialAppName") }
                    )
                }
                IntentCategory.LoginSucceeded -> {
                    scheduleNotification(this, 21, 0, ReflectionNotification::class.java)
                    scheduleNotification(this, 12, 0, EMANotification::class.java)
                }
                else -> {

                }
            }
        }

        // sendBroadcast(Intent(this, ReflectionNotification::class.java))
    }
}

