package com.demo.messageapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.demo.messageapp.databinding.ActivityHomeBinding
import com.demo.messageapp.utils.UserStatusManager
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var userStatusManager: UserStatusManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Kiểm tra người dùng đã đăng nhập
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("HomeActivity", "User not logged in")
            // TODO: Chuyển hướng về màn hình đăng nhập
            finish()
            return
        }

        // Khởi tạo UserStatusManager với userId
        val userId = currentUser.uid
        userStatusManager = UserStatusManager(userId)

        // Cập nhật trạng thái online ngay sau khi đăng nhập thành công
        userStatusManager.setOnline()

        // Theo dõi vòng đời Activity
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                userStatusManager.setOnline()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                userStatusManager.setOffline()
            }
        })
    }
}