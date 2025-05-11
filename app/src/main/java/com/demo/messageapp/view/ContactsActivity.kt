package com.demo.messageapp.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.messageapp.R
import com.demo.messageapp.databinding.ActivityContactsBinding
import com.demo.messageapp.databinding.ActivityHomeBinding
import com.demo.messageapp.view.adapter.ContactsAdapter
import com.demo.messageapp.view.adapter.ConversationAdapter

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: ContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        adapter = ContactsAdapter(emptyList()) { contact ->
            // TODO: Xử lý khi người dùng nhấn vào 1 cuộc trò chuyện
        }

        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.conversationRecyclerView.adapter = adapter


    }
}