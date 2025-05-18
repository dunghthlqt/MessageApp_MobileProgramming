package com.demo.messageapp.utils

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryConfig {
    private const val CLOUD_NAME = "dtrtgk0ff"
    private const val API_KEY = "643721862766188"
    private const val API_SECRET = "KE_kINfl2GhkBsS7FLrTebuxYYI"

    fun init(context: Context) {
        val config = hashMapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )
        MediaManager.init(context, config)
    }
}