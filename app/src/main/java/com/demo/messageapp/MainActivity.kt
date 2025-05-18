package com.demo.messageapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.demo.messageapp.utils.CloudinaryConfig
import com.demo.messageapp.view.WelcomeActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryConfig.init(this)
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
    }
}