package com.demo.messageapp.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.demo.messageapp.R
import com.demo.messageapp.databinding.ActivityVoiceCallBinding
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig


class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    private var agoraEngine: RtcEngine? = null
    private val PERMISSION_REQUEST_ID = 7
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private var isMuted = false
    private var isSpeakerOn = false
    private var isCallConnected = false
    private var callDuration = 0
    private val callDurationHandler = Handler(Looper.getMainLooper())
    private var callDurationRunnable: Runnable? = null

    private var channelName: String = ""
    private var callerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận thông tin từ intent
        channelName = intent.getStringExtra("CHANNEL_NAME") ?: "default_channel"
        callerName = intent.getStringExtra("CALLER_NAME") ?: "User"

        // Cập nhật UI
        binding.tvCallerName.text = callerName

        // Xử lý các nút điều khiển
        setupUIControls()

        // Kiểm tra quyền truy cập và khởi tạo Agora
        if (checkSelfPermission()) {
            initializeAndJoinChannel()
        }
    }

    private fun setupUIControls() {
        // Nút tắt/bật micro
        binding.btnMute.setOnClickListener {
            isMuted = !isMuted
            agoraEngine?.muteLocalAudioStream(isMuted)
            binding.btnMute.setImageResource(
                if (isMuted) R.drawable.mic_off else R.drawable.mic
            )
        }

        // Nút loa ngoài
        binding.btnSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            agoraEngine?.setEnableSpeakerphone(isSpeakerOn)
            binding.btnSpeaker.setImageResource(
                if (isSpeakerOn) R.drawable.volume_up else R.drawable.volume_down
            )
        }

        // Nút kết thúc cuộc gọi
        binding.btnEndCall.setOnClickListener {
            endCall()
        }

        // Nút quay lại
        binding.btnBackCall.setOnClickListener {
            endCall()
        }
    }

    private fun checkSelfPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQUEST_ID)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ID &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeAndJoinChannel()
        } else {
            showMessage("Quyền ghi âm bị từ chối")
            finish()
        }
    }

    private fun initializeAndJoinChannel() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = "5f5726416fa240faabbe83f334edc03d" // Thay bằng App ID Agora của bạn
            config.mEventHandler = object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                    runOnUiThread {
                        isCallConnected = true
                        binding.tvCallStatus.text = "Connected"
                        binding.tvCallDuration.visibility = View.VISIBLE
                        startCallDurationTimer()
                    }
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    runOnUiThread {
                        showMessage("Remote user joined")
                    }
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    runOnUiThread {
                        showMessage("Remote user left")
                        // Kết thúc cuộc gọi nếu người dùng khác ngắt kết nối
                        if (reason == Constants.USER_OFFLINE_QUIT) {
                            endCall()
                        }
                    }
                }

                override fun onError(err: Int) {
                    runOnUiThread {
                        showMessage("Error: $err")
                    }
                }
            }

            agoraEngine = RtcEngine.create(config)

            // Cấu hình Agora Engine
            agoraEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            agoraEngine?.enableAudio()

            // Thiết lập mặc định là loa nhỏ (không phải loa ngoài)
            agoraEngine?.setEnableSpeakerphone(false)

            // Tham gia kênh
            val token: String? = null // Nếu bạn không sử dụng token (bảo mật thấp)
            agoraEngine?.joinChannel(token, channelName, null, 0)

        } catch (e: Exception) {
            showMessage("Khởi tạo Agora thất bại: ${e.message}")
            finish()
        }
    }

    private fun startCallDurationTimer() {
        callDurationRunnable = object : Runnable {
            override fun run() {
                callDuration++
                val minutes = callDuration / 60
                val seconds = callDuration % 60
                binding.tvCallDuration.text = String.format("%02d:%02d", minutes, seconds)
                callDurationHandler.postDelayed(this, 1000)
            }
        }
        callDurationHandler.postDelayed(callDurationRunnable!!, 1000)
    }

    private fun stopCallDurationTimer() {
        callDurationRunnable?.let {
            callDurationHandler.removeCallbacks(it)
        }
    }

    private fun endCall() {
        agoraEngine?.leaveChannel()
        stopCallDurationTimer()
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.leaveChannel()
        stopCallDurationTimer()

        // Hủy Agora engine khi activity bị destroy
        RtcEngine.destroy()
        agoraEngine = null
    }
}