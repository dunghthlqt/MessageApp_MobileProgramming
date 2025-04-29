package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.demo.messageapp.R
import com.demo.messageapp.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPassword2: EditText
    private lateinit var signIn: Button
    private lateinit var signUp: TextView

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById(R.id.userEmail)
        editTextPassword = findViewById(R.id.password)
        editTextPassword2 = findViewById(R.id.password2)
        signIn = findViewById(R.id.signin)
        signUp = findViewById(R.id.signup)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        viewModel.registerResult.observe(this) { (success, message) ->
            if (success) {
                viewModel.sendEmailVerification()
                Toast.makeText(this, "Đã ...", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Lỗi đăng nhập: $message", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.sendEmailVerificationResult.observe(this) { (success, message) ->
            if (success) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Lỗi đăng nhập: $message", Toast.LENGTH_SHORT).show()
            }
        }
        signIn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        signUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val password2 = editTextPassword2.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@RegisterActivity, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@RegisterActivity, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password2)) {
                Toast.makeText(this@RegisterActivity, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password != password2)
            {
                Toast.makeText(this@RegisterActivity, "Enter Password Fail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.register(email, password)
        }
    }
}