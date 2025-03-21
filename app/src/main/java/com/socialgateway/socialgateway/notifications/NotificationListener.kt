package com.socialgateway.socialgateway.notifications

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.socialgateway.socialgateway.SocialGatewayApp
import com.socialgateway.socialgateway.data.model.SocialApps
import com.socialgateway.socialgateway.ui.AppGridActivity
import com.socialgateway.socialgateway.ui.IntentCategory


class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val socialApp = SocialApps.first { it.packageName == sbn.packageName }
            if (SocialGatewayApp.shouldReceivePrompt(socialApp)) {
                val title = sbn.notification.extras.getString("android.title")
                val text = sbn.notification.extras.getString("android.text")
                val icon = sbn.notification.smallIcon
                val intent = Intent(this, AppGridActivity::class.java).let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    it.putExtra("intentCategory", IntentCategory.OpenApp)
                    it.putExtra("socialAppName", socialApp.name)
                }

                Notifier.notify(NotificationAttributes(this, intent, title, text, icon))
                cancelNotification(sbn.key)
            }
        } catch (e: Exception) {

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}