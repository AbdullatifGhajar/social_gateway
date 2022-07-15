package com.socialgateway.socialgateway.notifications

import android.content.BroadcastReceiver
import android.content.Context
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
