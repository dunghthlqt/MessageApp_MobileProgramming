package com.demo.messageapp.utils

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import android.util.Log
import kotlinx.coroutines.tasks.await

class UserStatusManager(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()
    private val userStatusRef = db.collection("users").document(userId)
    private val heartbeatInterval = 2 * 60 * 1000L // 2 phút
    private var heartbeatTimer: Timer? = null

    fun setOnline() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userStatusRef.set(
                    mapOf(
                        "online" to true,
                        "lastSeen" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    convertTimestampToMillis()
                }
                startHeartbeat()
            } catch (e: Exception) {
                Log.e("UserStatusManager", "Error setting online: ${e.message}")
            }
        }
    }

    fun setOffline() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userStatusRef.set(
                    mapOf(
                        "online" to false,
                        "lastSeen" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    convertTimestampToMillis()
                }
                stopHeartbeat()
            } catch (e: Exception) {
                Log.e("UserStatusManager", "Error setting offline: ${e.message}")
            }
        }
    }

    private fun startHeartbeat() {
        stopHeartbeat() // Dừng heartbeat cũ nếu có
        heartbeatTimer = Timer()
        scheduleNextHeartbeat()
    }

    private fun scheduleNextHeartbeat() {
        heartbeatTimer?.schedule(object : TimerTask() {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        userStatusRef.update(
                            "lastSeen", FieldValue.serverTimestamp()
                        ).addOnSuccessListener {
                            // Chuyển đổi serverTimestamp thành milliseconds sau khi ghi
                            convertTimestampToMillis()
                        }
                        // Lên lịch cho heartbeat tiếp theo
                        scheduleNextHeartbeat()
                    } catch (e: Exception) {
                        Log.e("UserStatusManager", "Error updating heartbeat: ${e.message}")
                    }
                }
            }
        }, heartbeatInterval)
    }

    private fun stopHeartbeat() {
        heartbeatTimer?.cancel()
        heartbeatTimer = null
    }

    // Chuyển đổi giá trị lastSeen từ Timestamp thành milliseconds (Long)
    private fun convertTimestampToMillis() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = userStatusRef.get().await()
                val lastSeen = snapshot.getTimestamp("lastSeen")
                if (lastSeen != null) {
                    val millis = lastSeen.toDate().time
                    userStatusRef.update("lastSeen", millis)
                }
            } catch (e: Exception) {
                Log.e("UserStatusManager", "Error converting timestamp: ${e.message}")
            }
        }
    }
}