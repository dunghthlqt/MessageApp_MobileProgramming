package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.demo.messageapp.R
import com.demo.messageapp.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    private lateinit var login: Button

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)

        val editTextEmail: EditText = findViewById<TextInputEditText>(R.id.userEmail)
        val editTextPassword: EditText = findViewById<TextInputEditText>(R.id.password)

        login = findViewById(R.id.login)
        val signUp: TextView = findViewById(R.id.sign_up)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        viewModel.loginResult.observe(this) { (success, message) ->
            if (success) {
                if(viewModel.isUserVerified()) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    viewModel.sendEmailVerification()
                }
            } else {
                Toast.makeText(this, "Lỗi đăng nhập: $message", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.sendEmailVerificationResult.observe(this) { (success, message) ->
            if (success) {
                Toast.makeText(this, "Đã đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi đăng nhập: $message", Toast.LENGTH_SHORT).show()
            }
        }

        signUp.setOnClickListener {
            openRegisterPage()
        }



        login.setOnClickListener()
        {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

    }
    fun openRegisterPage() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
    override fun onBackPressed() {

    }
}