package com.socialgateway.socialgateway.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import socialgateway.socialgateway.R
import java.security.MessageDigest
import java.util.*
import kotlin.text.Charsets.UTF_8


class NotificationAttributes(
    val context: Context,
    val intent: Intent,
    val title: String?,
    val text: String?,
    val icon: IconCompat = IconCompat.createWithResource(context, R.drawable.ic_notification_icon)
) {
    companion object {
        operator fun invoke(
            context: Context,
            intent: Intent,
            title: String?,
            text: String?,
            icon: Icon
        ) = NotificationAttributes(
            context,
            intent,
            title,
            text,
            IconCompat.createFromIcon(context, icon)!!
        )
    }

    fun hash(): Int {
        val str = title + text
        val hashBytes = MessageDigest.getInstance("MD5").digest(str.toByteArray(UTF_8))

        var hash = 0
        for (i in hashBytes.indices)
            hash = (hash or (hashBytes[i].toInt() shl 8 * i)) % Int.MAX_VALUE

        return hash
    }
}

class Notifier {
    companion object {
        fun notify(attributes: NotificationAttributes) {
            Thread {
                try {
                    val pendingIntent = PendingIntent.getActivity(
                        attributes.context,
                        attributes.text.hashCode(),
                        attributes.intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    val builder = NotificationCompat.Builder(
                        attributes.context,
                        attributes.context.getString(R.string.notificationChannelId)
                    )
                        .setSmallIcon(attributes.icon)
                        .setContentTitle(attributes.title)
                        .setContentText(attributes.text)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(attributes.text))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    with(NotificationManagerCompat.from(attributes.context)) {
                        notify(attributes.hash(), builder.build())
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

fun scheduleNotification(context: Context, hour: Int, minute: Int, notificationClass: Class<*>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val dailyTriggerTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.timeInMillis

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, notificationClass),
        0,
    )

    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        dailyTriggerTime,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}