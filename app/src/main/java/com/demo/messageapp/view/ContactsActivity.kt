package com.demo.messageapp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.messageapp.R
import com.demo.messageapp.databinding.ActivityContactsBinding
import com.demo.messageapp.databinding.ActivityHomeBinding

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


    }
}