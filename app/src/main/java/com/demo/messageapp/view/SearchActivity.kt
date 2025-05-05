package com.demo.messageapp.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.messageapp.R
import com.demo.messageapp.viewmodel.UserViewModel
import com.demo.messageapp.view.MessagesAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize ViewModel and RecyclerView
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        recyclerView = findViewById(R.id.chatRecyclerView)
        searchView = findViewById(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessagesAdapter()
        recyclerView.adapter = messagesAdapter

        // Set up observer for search results
        userViewModel.searchUserbyNameResult.observe(this, { result ->
            if (result.success) {
                result.user?.messages?.let { messages ->
                    messagesAdapter.submitList(messages)  // Cập nhật danh sách tin nhắn
                }
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        userViewModel.searchUserbyUidResult.observe(this, { result ->
            if (result.success) {
                result.user?.messages?.let { messages ->
                    messagesAdapter.submitList(messages)
                }
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        userViewModel.searchUserbyEmailResult.observe(this, { result ->
            if (result.success) {
                result.user?.messages?.let { messages ->
                    messagesAdapter.submitList(messages)
                }
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        // Listen for search query changes
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchUser(it)  // Gọi hàm tìm kiếm khi người dùng nhấn Enter
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchUser(it)  // Gọi hàm tìm kiếm khi người dùng thay đổi nội dung tìm kiếm
                }
                return true
            }
        })
    }

    private fun searchUser(query: String) {
        // Kiểm tra xem tìm kiếm bằng tên, email hay UID và gọi phương thức tương ứng
        if (query.contains("@")) {
            // Nếu query chứa "@" có thể là email
            userViewModel.searchUserbyEmail(query)
        } else if (query.length == 24) {
            // Nếu query có độ dài 24 ký tự, giả sử đó là UID
            userViewModel.searchUserbyUid(query)
        } else {
            // Mặc định tìm kiếm theo tên
            userViewModel.searchUserbyName(query)
        }
    }
}
