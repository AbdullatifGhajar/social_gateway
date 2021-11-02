package com.example.socialgateway

class SocialApp(
    val name: String,
    val packageName: String,
    val imageId: Int
)

val SocialApps = listOf(
    SocialApp("Telegram", "org.telegram.messenger", R.drawable.telegram),
    SocialApp("WhatsApp", "com.whatsapp", R.drawable.whatsapp),
    SocialApp("Facebook", "com.facebook.katana", R.drawable.facebook),
    SocialApp("Facebook Messenger", "com.facebook.orca", R.drawable.facebook_messenger),
    SocialApp("Instagram", "com.instagram.android", R.drawable.instagram),
    SocialApp("Signal", "org.thoughtcrime.securesms", R.drawable.signal),
    SocialApp("Snapchat", "com.snapchat.android", R.drawable.snapchat)
)