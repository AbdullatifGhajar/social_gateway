package com.example.socialgateway

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat
import android.util.Log
import android.widget.*
import java.io.File
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import java.util.Calendar.*

// TODO find a better place for these
const val channelId = "SocialGatewayChannelId"

fun log(message: String) {
    Log.d("SocialGateway", message)
}

enum class IntentCategory { AskQuestion, Reflection, CheckIn }

class MainActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var preferences: SharedPreferences

    private fun requestQuestion(socialAppName: String, socialAppIntent: Intent? = null): String? {
        // make sure network request is not done on UI thread???
        assert(Looper.myLooper() != Looper.getMainLooper())

        // TODO do we need to give these information to server interface
        val language = if (Locale.getDefault().language == "de") "german" else "english"
        val questionType = if (socialAppIntent == null) "reflection" else "normal"


        try {
            return ServerInterface().getQuestion(socialAppName, language, questionType)
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
            log("could not request question: ${exception.message.orEmpty()}")
            finish()
            return null
        }
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

    private fun scheduleReflectionQuestion(socialAppName: String) {
        AsyncTask.execute {
            val question = requestQuestion(socialAppName)
                ?: return@execute  // TODO request reflection question

            runOnUiThread {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("intentCategory", IntentCategory.Reflection)
                    putExtra("question", question)
                    putExtra("socialAppName", socialAppName)
                }
                // TODO replace 0
                val pendingIntent = PendingIntent.getActivity(this, question.hashCode(), intent, 0)

                val builder = NotificationCompat.Builder(this, channelId)
                    // TODO change icon
                    .setSmallIcon(R.drawable.placeholder)
                    .setContentTitle(getString(R.string.reflection_question))
                    .setContentText(question)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(question))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                // show the reflection question notification with some minutes delay
                val minutes = 10
                Handler().postDelayed({
                    // notificationId is a unique int for each notification that you must define
                    val notificationId = System.currentTimeMillis() % Int.MAX_VALUE
                    NotificationManagerCompat.from(this)
                        .notify(notificationId.toInt(), builder.build())
                }, (minutes * 60 * 1000).toLong())
            }
        }
    }

    private fun today(): String? {
        return DateFormat.format("dd.MM.yyyy", Date()) as String?
    }

    // @SuppressLint("InflateParams")
    private fun showResponseDialog(
        question: String,
        socialApp: SocialApp
    ) {
        assert(question.isNotBlank())

        val linearLayout = layoutInflater.inflate(R.layout.answer_dialog, null)
        val answerEditText = linearLayout.findViewById<EditText>(R.id.answer_edit_text)
        val answerRecordAudioButton =
            linearLayout.findViewById<Button>(R.id.answer_record_audio_button)
        var mediaRecorder: MediaRecorder? = null

        answerRecordAudioButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // why 82? use other methods to request permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    82
                )
            } else {
                // TODO refactor branching
                when {
                    // TODO don't rely on text
                    answerRecordAudioButton.text == getString(R.string.delete_recording) -> {
                        getAnswerAudioFile().delete()
                        answerRecordAudioButton.text = getString(R.string.start_recording)
                    }
                    mediaRecorder == null -> {
                        mediaRecorder = startAudioRecording()
                        answerRecordAudioButton.text = getString(R.string.stop_recording)
                    }
                    else -> {
                        mediaRecorder?.apply {
                            stop()
                            release()
                        }
                        mediaRecorder = null
                        answerRecordAudioButton.text = getString(R.string.delete_recording)
                    }
                }
            }
        }

        AlertDialog.Builder(this).apply {
            setMessage(question)
            setView(linearLayout)
            setNegativeButton(android.R.string.cancel) { _, _ -> }
            setPositiveButton(android.R.string.ok) { _, _ ->
                mediaRecorder?.apply {
                    stop()
                    release()
                }

                // send the answer to the server and start the app
                ServerInterface().sendAnswer(
                    socialApp.name,
                    userId,
                    question,
                    answerEditText.text.toString(),
                    getAnswerAudioFile()
                )

                // socialAppIntent is null for reflection and check-in questions
                // TODO use if reflection or check-in instead
                if (socialApp != null) {
                    scheduleReflectionQuestion(socialApp.name)
                    startApp(socialApp)

                    // track when the question was answered, so more questions are asked for this app today
                    preferences.edit().apply {
                        putString(socialApp.name, today())
                        putString("lastQuestionDate", today())
                        val questionsOnLastQuestionDate =
                            preferences.getInt("questionsOnLastQuestionDate", 0)
                        putInt("questionsOnLastQuestionDate", questionsOnLastQuestionDate + 1)
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

    // check if the user was already asked a question for this app or two questions for any apps today
    private fun shouldReceiveQuestion(socialApp: SocialApp): Boolean {
        return (preferences.getString(socialApp.name, "") == today()
                || (preferences.getString("lastQuestionDate", "") == today()
                && preferences.getInt("questionsOnLastQuestionDate", 0) >= 2)
                )
    }

    private fun isInstalled(socialApp: SocialApp): Boolean{
        return packageManager.getLaunchIntentForPackage(socialApp.packageName) != null
    }

    // TODO take social app: SocialApp
    private fun askQuestion(socialApp: SocialApp) {
        val mainActivity = this
        val socialAppIntent = packageManager.getLaunchIntentForPackage(socialApp.packageName)

        if (!isInstalled(socialApp)) {
            resources.getString(R.string.X_was_not_found_on_your_device, socialApp.name).let {
                Toast.makeText(mainActivity, it, Toast.LENGTH_LONG).show()
            }
            // TODO don't load the whole activity. Use fragments instead
            finish()
            return
        }

        // TODO KATIE should the app close this way?
        if (!shouldReceiveQuestion(socialApp)) {
            finish()
            return
        }

        AsyncTask.execute {
            val question = requestQuestion(socialApp.name, socialAppIntent)
            if (question == null) {
                scheduleReflectionQuestion(socialApp.name)
                startActivity(socialAppIntent)
            } else {
                runOnUiThread {
                    showResponseDialog(question, socialApp)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.social_apps_grid)

        createNotificationChannel()

        preferences = getPreferences(Context.MODE_PRIVATE)

        userId = preferences.getString("userId", "").orEmpty().ifBlank {
            log("generating new userId")
            UUID.randomUUID().toString()
        }
        log("userId: $userId")

        findViewById<GridView>(R.id.social_apps_grid).adapter =
            SocialAppAdapter(this) { context, socialApp ->
                startActivity(Intent(context, MainActivity::class.java).apply {
                    putExtra("intentCategory", IntentCategory.AskQuestion)
                    putExtra("socialAppName", socialApp.name)
                    putExtra("socialAppPackageName", socialApp.packageName)
                })
            }

        intent?.extras?.let { intent ->

            when (intent.getSerializable("intentCategory") as? IntentCategory) {
                IntentCategory.AskQuestion -> {
                    askQuestion(
                        SocialApps.first { it.name == intent.getString("socialAppName") }
                    )
                }
                IntentCategory.Reflection -> {
                    showResponseDialog(
                        intent.getString("question").orEmpty(),
                        SocialApps.first { it.name == intent.getString("socialAppName") }
                    )
                }
                IntentCategory.CheckIn -> {
                    // TODO use a parameter mode = "check-in"
                    showResponseDialog(intent.getString("question").orEmpty(), SocialApps[0])
                }
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dailyTriggerTime = Calendar.getInstance().apply {
            set(HOUR_OF_DAY, 21)
            set(MINUTE, 0)
            set(SECOND, 0)
        }.timeInMillis

        val pendingIntent = PendingIntent.getBroadcast(
            this, 346538746,
            Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setRepeating(
            AlarmManager.RTC,
            dailyTriggerTime,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun getAnswerAudioFile(): File {
        return cacheDir.resolve("social_gateway_answer_audio.aac")
    }

    private fun startAudioRecording(): MediaRecorder {
        return MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(getAnswerAudioFile().absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        preferences.edit().apply {
            putString("userId", userId)
            apply()
        }
    }
}
