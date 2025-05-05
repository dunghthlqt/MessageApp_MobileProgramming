package com.demo.messageapp.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.messageapp.R
import com.demo.messageapp.viewmodel.MessageViewModel
import com.demo.messageapp.view.MessagesAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var noMessagesTextView: TextView
    private lateinit var addMessage: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        messagesAdapter = MessagesAdapter()
        messagesRecyclerView.adapter = messagesAdapter

        noMessagesTextView = findViewById(R.id.noMessagesTextView)
        addMessage = findViewById(R.id.btnAddMessage)

        messageViewModel.getMessageListResult.observe(this, Observer { result ->
            if (result.success) {
                result.messageList?.let {
                    if (it.isEmpty()) {
                        messagesRecyclerView.visibility = View.GONE
                        noMessagesTextView.visibility = View.VISIBLE
                    } else {
                        messagesRecyclerView.visibility = View.VISIBLE
                        noMessagesTextView.visibility = View.GONE
                        messagesAdapter.submitList(it)
                    }
                }
            }
        })

        messageViewModel.getMessageList("123")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_icon -> {
                Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
