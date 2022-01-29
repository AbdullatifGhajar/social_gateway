package com.socialgateway.socialgateway

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.socialgateway.socialgateway.ui.login.LoginActivity
import socialgateway.socialgateway.R
import java.net.UnknownHostException
import java.util.*
import java.util.Calendar.*

fun log(message: String) {
    Log.d("SocialGateway", message)
}

fun today(): String {
    return DateFormat.format("dd.MM.yyyy", Date()) as String
}

enum class IntentCategory { AskQuestion, Reflection }

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

    // check if the user already a prompt for this app today
    private fun shouldReceivePrompt(socialApp: SocialApp): Boolean {
        return (preferences.getString("last_prompt:${socialApp.name}", "") != today())
    }

    private fun isInstalled(socialApp: SocialApp): Boolean {
        return packageManager.getLaunchIntentForPackage(socialApp.packageName) != null
    }

    private fun showResponseDialog(prompt: Prompt, socialApp: SocialApp?) {
        AnswerDialog(this, socialApp, prompt,
            onSubmit = {
                if (socialApp != null) {
                    startApp(socialApp)
                    // log this for shouldReceivePrompt later
                    preferences.edit().apply {
                        putString("last_prompt:${socialApp.name}", today())
                        putString("lastPromptDate", today())
                        apply()
                    }
                }
            }, onCancel = { })
    }

    private fun chooseApp(socialApp: SocialApp) {
        if (!isInstalled(socialApp)) {
            resources.getString(R.string.X_was_not_found_on_your_device, socialApp.name).let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
            return
        }

        /*
        if (!shouldReceivePrompt(socialApp)) {
            startApp(socialApp)
            return
        } */


        val prompt = requestPrompt(socialApp)
        if (prompt == null) {
            startApp(socialApp)
        } else {
            showResponseDialog(prompt, socialApp)
        }
    }

    private fun authenticateUser() {
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = preferences.getString("userId", "").toString()

        if (userId.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authenticateUser()

        setContentView(R.layout.social_apps_grid)
        createNotificationChannel(this)

        findViewById<GridView>(R.id.social_apps_grid).adapter =
            SocialAppAdapter(this) { _, socialApp ->
                chooseApp(socialApp)
            }

        intent?.extras?.let { intent ->

            when (intent.getSerializable("intentCategory") as? IntentCategory) {
                IntentCategory.AskQuestion -> {
                    chooseApp(
                        SocialApps.first { it.name == intent.getString("socialAppName") }
                    )
                }
                IntentCategory.Reflection -> {
                    val prompt = Prompt(intent.getString("question").orEmpty(), true)
                    showResponseDialog(prompt, null)
                }
                else -> {
                    // do nothing
                }
            }
        }

        // sendBroadcast(Intent(this, ReflectionNotification::class.java))

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dailyTriggerTime = getInstance().apply {
            set(HOUR_OF_DAY, 21)
            set(MINUTE, 0)
            set(SECOND, 0)
        }.timeInMillis

        val pendingIntent = getBroadcast(
            this,
            0,
            Intent(this, ReflectionNotification::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            dailyTriggerTime,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onPause() {
        // TODO: why?
        super.onPause()
        preferences.edit().apply {
            putString("userId", userId)
            apply()
        }
    }
}

