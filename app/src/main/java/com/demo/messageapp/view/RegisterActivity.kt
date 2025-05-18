package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
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

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPassword2: EditText
    private lateinit var signUp: Button

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById(R.id.userEmail)
        editTextPassword = findViewById(R.id.password)
        editTextPassword2 = findViewById(R.id.password2)
        val signIn: TextView = findViewById(R.id.signin)
        signUp = findViewById(R.id.signup)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        viewModel.registerResult.observe(this) { (success, message) ->
            if (success) {
                viewModel.sendEmailVerification()
                Toast.makeText(this, "Đã đăng ký thành công, vui lòng kiểm tra email để xác minh.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi đăng ký: $message", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.sendEmailVerificationResult.observe(this) { (success, message) ->
            if (success) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // Đảm bảo quay lại trang đăng nhập ngay sau khi gửi email xác minh
            } else {
                Toast.makeText(this, "Lỗi gửi email xác minh: $message", Toast.LENGTH_SHORT).show()
            }
        }

        // Chuyển đến trang đăng nhập khi người dùng nhấn vào "Sign In"
        signIn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Xử lý sự kiện nhấn nút Đăng ký
        signUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val password2 = editTextPassword2.text.toString()

            // Kiểm tra các trường thông tin nhập
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@RegisterActivity, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this@RegisterActivity, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@RegisterActivity, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password2)) {
                Toast.makeText(this@RegisterActivity, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != password2) {
                Toast.makeText(this@RegisterActivity, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi phương thức đăng ký trong ViewModel
            viewModel.register(email, password)
        }
    }

    // Chuyển tới trang đăng nhập khi nhấn vào "Sign In"
    fun openLoginPage(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    override fun onBackPressed() {

    }
}
