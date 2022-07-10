package com.socialgateway.socialgateway.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.socialgateway.socialgateway.ServerInterface
import com.socialgateway.socialgateway.SocialGatewayApp
import com.socialgateway.socialgateway.data.model.PromptType
import com.socialgateway.socialgateway.ui.AppGridActivity
import com.socialgateway.socialgateway.ui.IntentCategory
import socialgateway.socialgateway.R
import java.util.*

class ReflectionNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (SocialGatewayApp.shouldReceiveReflectionPrompt()) {
            val prompt = ServerInterface.getPrompt(null, PromptType.REFLECTION)
            assert(prompt.answerable)
            val reflectionIntent = Intent(context, AppGridActivity::class.java).let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                it.putExtra("intentCategory", IntentCategory.Reflection)
                it.putExtra("question", prompt.content)
            }
            Notifier.notify(
                NotificationAttributes(
                    context,
                    reflectionIntent,
                    context.resources.getString(R.string.reflection_question),
                    prompt.content
                )
            )
        }
    }
}

fun scheduleReflectionNotification(context: Context){
    val hour = 21
    val minute = 0
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    val dailyTriggerTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.timeInMillis

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, ReflectionNotification::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        dailyTriggerTime,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}
