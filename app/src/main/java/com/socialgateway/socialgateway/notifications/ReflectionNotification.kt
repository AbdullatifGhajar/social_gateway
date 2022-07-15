package com.socialgateway.socialgateway.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.socialgateway.socialgateway.ServerInterface
import com.socialgateway.socialgateway.SocialGatewayApp
import com.socialgateway.socialgateway.data.model.PromptType
import com.socialgateway.socialgateway.ui.AppGridActivity
import com.socialgateway.socialgateway.ui.IntentCategory
import socialgateway.socialgateway.R

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

