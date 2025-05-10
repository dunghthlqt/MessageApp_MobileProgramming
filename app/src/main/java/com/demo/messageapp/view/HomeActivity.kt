package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.messageapp.databinding.ActivityHomeBinding
import com.demo.messageapp.view.adapter.ConversationAdapter
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.ConversationViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        conversationViewModel = ViewModelProvider(this)[ConversationViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        adapter = ConversationAdapter(emptyList()) { conversation ->
            // TODO: Xử lý khi người dùng nhấn vào 1 cuộc trò chuyện
        }

        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.conversationRecyclerView.adapter = adapter

        var currentUid: String = ""

        authViewModel.currentUser.observe(this, Observer { result ->
            if (result != null) {
                currentUid = result.uid
            }
            conversationViewModel.getConversationList(currentUid)
        })

        conversationViewModel.getConversationListResult.observe(this, Observer { result ->
            if (result.success) {
                result.conversationList?.let {
                    if (it.isEmpty()) {
                        binding.conversationRecyclerView.visibility = View.GONE
                        binding.noMessagesTextView.visibility = View.VISIBLE
                    } else {
                        binding.conversationRecyclerView.visibility = View.VISIBLE
                        binding.noMessagesTextView.visibility = View.GONE

                        adapter.updateData(result.conversationList)
                    }
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        authViewModel.getCurrentUser()

        binding.btnSearch.setOnClickListener{
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.btnContacts.setOnClickListener{
            Toast.makeText(this, "Contacts clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnSetting.setOnClickListener{
            Toast.makeText(this, "Setting clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
