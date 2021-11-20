package com.example.socialgateway

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat
import android.util.Log
import android.widget.*
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import java.util.Calendar.*


// TODO: find a better place for these
const val channelId = "SocialGatewayChannelId"

fun log(message: String) {
    Log.d("SocialGateway", message)
}

private fun today(): String {
    return DateFormat.format("dd.MM.yyyy", Date()) as String
}

enum class IntentCategory { AskQuestion, Reflection, CheckIn }

class MainActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var preferences: SharedPreferences
    private lateinit var recorder: VoiceRecorder

    private fun requestPrompt(socialApp: SocialApp, promptType: String = "normal"): Prompt? {
        // make sure network request is not done on UI thread???
        assert(Looper.myLooper() != Looper.getMainLooper())

        try {
            return ServerInterface().getPrompt(socialApp, promptType)
        } catch (exception: Exception) {
            val errorMessage = resources.getString(
                when (exception) {
                    is ConnectException -> R.string.server_unreachable
                    is UnknownHostException -> R.string.no_internet_connection_available
                    else -> R.string.unknown_error
                }
            )
            runOnUiThread {
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
            log("could not request prompt: ${exception.message.orEmpty()}")
        }
        return null
    }

    private fun createNotificationChannel() { // https://developer.android.com/training/notify-user/build-notification
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showResponseDialog(
        prompt: Prompt,
        socialApp: SocialApp?
    ) {
        val linearLayout = layoutInflater.inflate(R.layout.answer_dialog, null)
        val recordingLayout = linearLayout.findViewById<LinearLayout>(R.id.recording_layout)
        val typingLayout = linearLayout.findViewById<LinearLayout>(R.id.typing_layout)

        recordingLayout.visibility = LinearLayout.GONE

        // TODO remove the two if statements (here and below)
        if (prompt.answerable) {
            recorder = VoiceRecorder(this, linearLayout)

            linearLayout.findViewById<ImageButton>(R.id.record_button).setOnClickListener {
                typingLayout.visibility = EditText.GONE
                recordingLayout.visibility = LinearLayout.VISIBLE
            }

            linearLayout.findViewById<ImageButton>(R.id.text_button).setOnClickListener {
                typingLayout.visibility = EditText.VISIBLE
                recordingLayout.visibility = LinearLayout.INVISIBLE
            }
        } else {
            typingLayout.visibility = LinearLayout.GONE
        }

        AlertDialog.Builder(this).apply {
            setMessage(prompt.content)
            setView(linearLayout)
            setNegativeButton(android.R.string.cancel) { _, _ ->
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                // TODO stop recording or playing
                if (prompt.answerable) {
                    // send the answer to the server and start the app
                    ServerInterface().sendAnswer(
                        // TODO KATIE how should check-in work
                        socialApp?.name ?: "check-in",
                        userId,
                        prompt.content,
                        linearLayout.findViewById<TextView>(R.id.answer_edit_text).text.toString(),
                        recorder.recordingFile()
                    )
                }

                if (socialApp != null) {
                    startApp(socialApp)

                    // track when the question was answered, so more questions are asked for this app today
                    preferences.edit().apply {
                        putString("last_prompt:${socialApp.name}", today())
                        putString("lastQuestionDate", today())
                        apply()
                    }
                }
            }
            create()
            show()
        }
    }

    private fun startApp(socialApp: SocialApp) {
        startActivity(packageManager.getLaunchIntentForPackage(socialApp.packageName))
    }

    // check if the user was already asked a question for this app
    private fun shouldReceivePrompt(socialApp: SocialApp): Boolean {
        return (preferences.getString("last_prompt:${socialApp.name}", "") != today())
    }

    private fun isInstalled(socialApp: SocialApp): Boolean {
        return packageManager.getLaunchIntentForPackage(socialApp.packageName) != null
    }

    private fun chooseApp(socialApp: SocialApp) {
        if (!isInstalled(socialApp)) {
            resources.getString(R.string.X_was_not_found_on_your_device, socialApp.name).let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
            return
        }

        // TODO: KATIE should the app close this way?
        /* if (!shouldReceiveQuestion(socialApp)) {
            startApp(socialApp)
            return
        } */

        AsyncTask.execute {
            val prompt = requestPrompt(socialApp)
            if (prompt == null) {
                startApp(socialApp)
            } else {
                runOnUiThread {
                    showResponseDialog(prompt, socialApp)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.social_apps_grid)

        createNotificationChannel()

        preferences = getPreferences(Context.MODE_PRIVATE)

        userId = preferences.getString("userId", "").ifEmpty {
            log("generating new userId")
            UUID.randomUUID().toString()
        }
        log("userId: $userId")

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
                    // TODO take a look at this
                    val prompt = Prompt(intent.getString("question").orEmpty(), true)
                    showResponseDialog(prompt, null)
                }
                else -> {
                    // do nothing
                }
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dailyTriggerTime = getInstance().apply {
            set(HOUR_OF_DAY, 21)
            set(MINUTE, 0)
            set(SECOND, 0)
        }.timeInMillis

        // TODO: add question to it
        val pendingIntent = getBroadcast(
            this, 346538746,
            Intent(this, AlarmReceiver::class.java), FLAG_UPDATE_CURRENT
        )

        alarmManager.setRepeating(
            AlarmManager.RTC,
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

// TODO:
//  - reflection only at 9pm
//  - brainstorm name
//  - refactor question to prompt
//  - server returns prompt body and isQuestion

