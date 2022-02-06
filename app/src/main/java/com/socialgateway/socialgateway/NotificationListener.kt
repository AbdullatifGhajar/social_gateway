package com.socialgateway.socialgateway

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification


class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val socialApp = SocialApps.first { it.packageName == sbn.packageName }
            val title = "Notification from ${socialApp.name}"
            val text = sbn.notification.extras.getString("android.text")
            val intent = Intent(this, AppGridActivity::class.java).let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                it.putExtra("intentCategory", IntentCategory.OpenApp)
                it.putExtra("socialAppName", socialApp.name)
            }
            Notifier.notify(NotificationAttributes(this, intent, title, text!!, socialApp.name))
            cancelNotification(sbn.key)
        } catch (e: Exception) {

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}