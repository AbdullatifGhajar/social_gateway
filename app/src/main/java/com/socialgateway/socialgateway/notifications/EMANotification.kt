package com.socialgateway.socialgateway.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.socialgateway.socialgateway.SocialGatewayApp
import com.socialgateway.socialgateway.ui.AppGridActivity
import com.socialgateway.socialgateway.ui.IntentCategory


class EMANotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (SocialGatewayApp.shouldReceiveEMAPrompt()) {
            val emaQuestion = Intent(context, AppGridActivity::class.java).let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                it.putExtra("intentCategory", IntentCategory.EMA)
            }
            Notifier.notify(
                NotificationAttributes(
                    context,
                    emaQuestion,
                    "EMA of the Day",
                    "Tap to answer"
                )
            )
        }
    }
}

fun scheduleEMANotification(context: Context) {
    val minutes = 1
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, EMANotification::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
    )

    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis() + 1000 * 60 * minutes,
        pendingIntent
    )

}
