package com.demo.messageapp.view
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.messageapp.R
import com.demo.messageapp.viewmodel.MessageViewModel
import com.demo.messageapp.view.MessagesAdapter

class HomeActivity : AppCompatActivity() {
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        // Khởi tạo RecyclerView và adapter
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        messagesAdapter = MessagesAdapter() // Sử dụng MessagesAdapter ở đây
        messagesRecyclerView.adapter = messagesAdapter

        // Quan sát dữ liệu từ ViewModel
        messageViewModel.getMessageListResult.observe(this, Observer { result ->
            if (result.success) {
                // Cập nhật adapter với danh sách tin nhắn
                result.messageList?.let {
                    // Truyền danh sách tin nhắn vào adapter
                    messagesAdapter.submitList(it) // Cập nhật adapter với dữ liệu mới
                }
            }
        })

        // Gọi phương thức getMessageList để lấy dữ liệu tin nhắn
        messageViewModel.getMessageList("123") // Sử dụng ID cuộc trò chuyện mà bạn muốn lấy tin nhắn
    }
}
