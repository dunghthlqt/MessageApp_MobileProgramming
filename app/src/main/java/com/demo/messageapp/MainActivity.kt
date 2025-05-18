package com.demo.messageapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.demo.messageapp.utils.CloudinaryConfig
import com.demo.messageapp.view.HomeActivity
import com.demo.messageapp.view.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryConfig.init(this)
        if(FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
    }
}