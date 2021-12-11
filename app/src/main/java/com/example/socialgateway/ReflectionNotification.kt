package com.example.socialgateway

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

class ReflectionNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val question = context.resources.getString(R.string.check_in_question_value)

        val reflectionIntent = Intent(context, MainActivity::class.java).let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.putExtra("intentCategory", IntentCategory.Reflection)
            it.putExtra("question", question)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            "reflection".hashCode(),
            reflectionIntent,
            0
        )

        val builder = NotificationCompat.Builder(
            context,
            context.getString(R.string.notificationChannelId)
        )
            .setSmallIcon(R.drawable.placeholder)
            .setContentTitle(context.resources.getString(R.string.check_in_question))
            .setContentText(question)
            .setStyle(NotificationCompat.BigTextStyle().bigText(question))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // notificationId is a unique int for each notification that you must define
        val notificationId = System.currentTimeMillis() % Int.MAX_VALUE
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId.toInt(), builder.build())
        }
    }
}

// Create the NotificationChannel, but only on API 26+ because
// the NotificationChannel class is new and not in the support library
// https://developer.android.com/training/notify-user/build-notification
fun createNotificationChannel(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = activity.getString(R.string.channel_name)
        val descriptionText = activity.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannelId = activity.getString(R.string.notificationChannelId)

        val channel = NotificationChannel(
            notificationChannelId,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}