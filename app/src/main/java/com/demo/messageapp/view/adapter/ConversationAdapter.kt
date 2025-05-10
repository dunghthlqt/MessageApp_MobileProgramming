package com.demo.messageapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.messageapp.R
import com.demo.messageapp.databinding.ItemConversationBinding
import com.demo.messageapp.model.Conversation
//import com.amulyakhare.textdrawable.TextDrawable
//import com.amulyakhare.textdrawable.util.ColorGenerator

class ConversationAdapter(
    private var conversations: List<Conversation>,
    private val onConversationClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            val name = conversation.conversationName ?: "?"
//            val avatarUrl = conversation.avatarUrl // giả định bạn có avatarUrl trong Conversation

            val avatarUrl = ""

            // Load ảnh avatar
//            if (!avatarUrl.isNullOrEmpty()) {
//                Glide.with(binding.root.context)
//                    .load(avatarUrl)
////                    .placeholder(R.drawable.default_avatar)
//                    .into(binding.avatar)
//            }

            binding.userName.text = name
            binding.messageText.text = conversation.lastMessage ?: ""

            binding.root.setOnClickListener {
                onConversationClick(conversation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size

    fun updateData(newConversations: List<Conversation>) {
        conversations = newConversations
        notifyDataSetChanged()
    }
}
