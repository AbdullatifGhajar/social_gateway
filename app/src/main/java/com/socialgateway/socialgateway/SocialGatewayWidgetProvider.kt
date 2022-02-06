package com.socialgateway.socialgateway

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import socialgateway.socialgateway.R

val appWidgetIdToSocialApp = mutableMapOf<Int, SocialApp>()

class SocialGatewayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val socialApp = appWidgetIdToSocialApp[appWidgetId] ?: return

            val pendingIntent = Intent(context, AppGridActivity::class.java).let {
                it.putExtra("intentCategory", IntentCategory.AskQuestion)
                it.putExtra("socialAppName", socialApp.name)
                it.putExtra("socialAppPackageName", socialApp.packageName)

                PendingIntent.getActivity(context, appWidgetId, it, 0)
            }

            RemoteViews(context.packageName, R.layout.widget).let {
                it.setImageViewResource(R.id.widget_button, socialApp.icon)
                it.setOnClickPendingIntent(R.id.widget_button, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, it)
            }
        }
    }
}
