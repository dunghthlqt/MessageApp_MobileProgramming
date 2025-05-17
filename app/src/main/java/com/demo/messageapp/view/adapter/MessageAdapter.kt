package com.demo.messageapp.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.messageapp.databinding.ItemMessageReceivedBinding
import com.demo.messageapp.databinding.ItemMessageSentBinding
import com.demo.messageapp.databinding.ItemReplyMessageReceivedBinding
import com.demo.messageapp.databinding.ItemReplyMessageSentBinding
import com.demo.messageapp.model.Message
import com.demo.messageapp.model.Reaction
import com.demo.messageapp.view.dialog.MessageOptionsDialog
import com.demo.messageapp.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private var messages: List<Message>,
    private val context: Context,
    private val userUid: String,
    private val userViewModel: UserViewModel,
    private val onReplyListener: (Message) -> Unit,
    private val onDeleteListener: (Message) -> Unit,
    private val onReactionAddedListener: (Message, String) -> Unit,
    private val onOriginalMessageClickedListener: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun getMessages(): List<Message> = messages
    private val userNameCache = mutableMapOf<String, String>()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_REPLY_SENT = 3
        private const val VIEW_TYPE_REPLY_RECEIVED = 4
    }

    inner class SentMessageViewHolder(val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewTime.text = formatTime(message.timestamp)
            binding.textViewMessage.text = message.content
            binding.cardViewMessage.setOnClickListener {
                showMessageOptions(message)
            }
            bindReactions(binding.reactionsContainer, message.reactions)
        }
    }

    inner class ReceivedMessageViewHolder(val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewTime.text = formatTime(message.timestamp)
            binding.textViewMessage.text = message.content
            binding.cardViewMessage.setOnClickListener {
                showMessageOptions(message)
            }
            bindReactions(binding.reactionsContainer, message.reactions)
        }
    }

    inner class SentReplyMessageViewHolder(val binding: ItemReplyMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewTime.text = formatTime(message.timestamp)
            binding.textViewMessage.text = message.content
            message.replyInfo?.let { replyInfo ->
                val cachedName = userNameCache[replyInfo.originalSenderId]
                if (cachedName != null) {
                    binding.repliedMessageSender.text = if (replyInfo.originalSenderId == userUid) "You" else cachedName
                } else {
                    userViewModel.getUserNameByUid(message.replyInfo.originalSenderId) { name ->
                        if (name != null) {
                            userNameCache[replyInfo.originalSenderId] = name
                            binding.repliedMessageSender.text = if (replyInfo.originalSenderId == userUid) "You" else name
                        }
                    }
                }
                binding.repliedMessageContent.text = replyInfo.replyContent
                binding.repliedMessageContainer.setOnClickListener {
                    onOriginalMessageClickedListener(replyInfo.originalMessageId)
                }
            }
            binding.cardViewMessage.setOnClickListener {
                showMessageOptions(message)
            }
            bindReactions(binding.reactionsContainer, message.reactions)
        }
    }

    inner class ReceivedReplyMessageViewHolder(val binding: ItemReplyMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewTime.text = formatTime(message.timestamp)
            binding.textViewMessage.text = message.content
            message.replyInfo?.let { replyInfo ->
                val cachedName = userNameCache[replyInfo.originalSenderId]
                if (cachedName != null) {
                    binding.repliedMessageSender.text = if (replyInfo.originalSenderId == userUid) "You" else cachedName
                } else {
                    userViewModel.getUserNameByUid(message.replyInfo.originalSenderId) { name ->
                        if (name != null) {
                            userNameCache[replyInfo.originalSenderId] = name
                            binding.repliedMessageSender.text = if (replyInfo.originalSenderId == userUid) "You" else name
                        }
                    }
                }
                binding.repliedMessageContent.text = replyInfo.replyContent
                binding.repliedMessageContainer.setOnClickListener {
                    onOriginalMessageClickedListener(replyInfo.originalMessageId)
                }
            }
            binding.cardViewMessage.setOnClickListener {
                showMessageOptions(message)
            }
            bindReactions(binding.reactionsContainer, message.reactions)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByMe) {
            if(messages[position].replyInfo == null) {
                VIEW_TYPE_SENT
            } else {
                VIEW_TYPE_REPLY_SENT
            }
        } else {
            if(messages[position].replyInfo == null) {
                VIEW_TYPE_RECEIVED
            } else {
                VIEW_TYPE_REPLY_RECEIVED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedMessageViewHolder(binding)
            }
            VIEW_TYPE_REPLY_SENT -> {
                val binding = ItemReplyMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SentReplyMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemReplyMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedReplyMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is SentReplyMessageViewHolder -> holder.bind(message)
            is ReceivedReplyMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateData(newMessages: List<Message>) {
        messages = newMessages.filter { !it.deleted }
        notifyDataSetChanged()
    }

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun showMessageOptions(message: Message) {
        val optionsDialog = MessageOptionsDialog(
            context = context,
            message = message,
            onReplyListener = onReplyListener,
            onDeleteListener = onDeleteListener,
            onReactionAddedListener = onReactionAddedListener
        )
        optionsDialog.show()
    }
    private fun bindReactions(container: LinearLayout, reactions: List<Reaction>) {
        // Xóa các view cũ
        container.removeAllViews()

        // Ẩn container nếu không có reactions
        container.visibility = if (reactions.isEmpty()) View.GONE else View.VISIBLE

        // Thêm TextView cho mỗi emoji
        reactions.forEach { reaction ->
            val emojiView = TextView(context).apply {
                text = reaction.emoji
                textSize = 16f
                setPadding(4, 0, 4, 0)
            }
            container.addView(emojiView)
        }
    }
}