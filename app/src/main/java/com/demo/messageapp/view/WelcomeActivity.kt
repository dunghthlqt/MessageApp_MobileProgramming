package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.demo.messageapp.R

class WelcomeActivity : AppCompatActivity() {
    private lateinit var btnStart: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        btnStart = findViewById(R.id.start)
        btnStart.setOnClickListener()
        {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}