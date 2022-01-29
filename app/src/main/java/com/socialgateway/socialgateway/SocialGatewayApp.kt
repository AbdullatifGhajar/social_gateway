package com.socialgateway.socialgateway

import android.app.Application
import android.content.res.Resources


class SocialGatewayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: SocialGatewayApp? = null
        val resources: Resources?
            get() = instance?.resources
    }
}