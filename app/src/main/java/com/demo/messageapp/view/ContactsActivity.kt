package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.messageapp.databinding.ActivityContactsBinding
import com.demo.messageapp.view.adapter.ContactsAdapter
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.UserViewModel

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: ContactsAdapter
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        adapter = ContactsAdapter(emptyList()) { contact ->
            // TODO: Xử lý khi người dùng nhấn vào 1 cuộc trò chuyện
        }

        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.conversationRecyclerView.adapter = adapter

        userViewModel.getUserListResult.observe(this, Observer { result ->
            if (result.success) {
                result.userList?.let {

                    for(user in result.userList) {
                        Log.d("User", "Error = ${user.displayName}")
                    }

                    adapter.updateData(result.userList)
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        userViewModel.getContactsUIDListResult.observe(this, Observer { result ->
            if (result.success) {
                result.contactsUIDList?.let {
                    if (it.isEmpty()) {
                        binding.conversationRecyclerView.visibility = View.GONE
                        binding.noMessagesTextView.visibility = View.VISIBLE
                    } else {
                        binding.conversationRecyclerView.visibility = View.VISIBLE
                        binding.noMessagesTextView.visibility = View.GONE

                        userViewModel.getUserList(result.contactsUIDList)
                    }
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        var currentUid: String = ""

        authViewModel.currentUser.observe(this, Observer { result ->
            if (result != null) {
                currentUid = result.uid
            }
            userViewModel.getContactsUIDList(currentUid)
        })

        authViewModel.getCurrentUser()

        binding.btnChat.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}