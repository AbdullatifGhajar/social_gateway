package com.socialgateway.socialgateway

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import socialgateway.socialgateway.R


class NotificationAttributes(
    val context: Context,
    val intent: Intent,
    val title: String,
    val text: String,
    val type: String
)

class Notifier {
    companion object {
        fun notify(attributes: NotificationAttributes) {
            Thread {
                try {
                    val pendingIntent = PendingIntent.getActivity(
                        attributes.context,
                        attributes.type.hashCode(),
                        attributes.intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )

                    val builder = NotificationCompat.Builder(
                        attributes.context,
                        attributes.context.getString(R.string.notificationChannelId)
                    )
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(attributes.title)
                        .setContentText(attributes.text)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(attributes.text))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    // notificationId is a unique int for each notification that you must define
                    val notificationId = System.currentTimeMillis() % Int.MAX_VALUE
                    with(NotificationManagerCompat.from(attributes.context)) {
                        notify(notificationId.toInt(), builder.build())
                    }
                } catch (e: Exception) {
                    // something went wrong, don't do a notification
                }
            }.start()
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        // https://developer.android.com/training/notify-user/build-notification
        fun createNotificationChannel(activity: Activity) {
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
}