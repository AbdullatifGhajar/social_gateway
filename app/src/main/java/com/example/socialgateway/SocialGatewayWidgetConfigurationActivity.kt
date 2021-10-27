package com.example.socialgateway

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.GridView

class SocialGatewayWidgetConfigurationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.social_apps_grid)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        findViewById<GridView>(R.id.social_apps_grid).adapter =
            SocialAppAdapter(this) { context, socialApp ->
                appWidgetIdToSocialApp[appWidgetId] = socialApp

                AppWidgetManager.getInstance(context).let {
                    SocialGatewayWidgetProvider.updateAppWidget(context, it, appWidgetId)
                }

                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                })

                finish()
            }
    }
}