package com.example.socialgateway

class SocialApp(
    val name: String,
    val packageName: String,
    val imageId: Int
)
// remember to include every app in manifest
val SocialApps = listOf(
    SocialApp("Snapchat", "com.snapchat.android", R.drawable.snapchat),
    SocialApp("TikTok", "com.zhiliaoapp.musically", R.drawable.tiktok),
    SocialApp("Instagram", "com.instagram.android", R.drawable.instagram),
    SocialApp("Twitter", "com.twitter.android", R.drawable.twitter),
    SocialApp("Discord", "com.discord", R.drawable.discord),
    SocialApp("Reddit", "com.reddit.frontpage", R.drawable.reddit),
    SocialApp("Tumblr", "com.tumblr", R.drawable.tumblr),
    SocialApp("WhatsApp", "com.whatsapp", R.drawable.whatsapp),
    SocialApp("Facebook", "com.facebook.katana", R.drawable.facebook),
    SocialApp("Facebook Messenger", "com.facebook.orca", R.drawable.facebook_messenger),
    SocialApp("Youtube", "com.google.android.youtube", R.drawable.youtube)
    // SocialApp("Telegram", "org.telegram.messenger", R.drawable.telegram),
    // SocialApp("Signal", "org.thoughtcrime.securesms", R.drawable.signal)
)