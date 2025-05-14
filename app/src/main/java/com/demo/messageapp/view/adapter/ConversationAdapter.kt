package com.demo.messageapp.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.messageapp.R
import com.demo.messageapp.databinding.ItemConversationBinding
import com.demo.messageapp.model.Conversation
import com.demo.messageapp.model.User
import com.demo.messageapp.model.resultmodel.SearchUserResult
import com.demo.messageapp.viewmodel.ConversationViewModel
import com.demo.messageapp.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ConversationAdapter(
    private var conversations: List<Conversation>,
    private var currentUserUid: String,
    private val userViewModel: UserViewModel,
    private val onConversationClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    // Map lưu thông tin người dùng theo uid
    private val userCache = mutableMapOf<String, User?>()
    // Map lưu các observer theo viewHolder position
    private val observers = mutableMapOf<Int, Observer<SearchUserResult>>()

    inner class ConversationViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation, position: Int) {
            if(conversation.conversationName != ""){
                binding.userName.text = conversation.conversationName
            } else {
                val otherUserId = conversation.participantIds.firstOrNull { it != currentUserUid }
                Log.d("Home", "Other = $otherUserId")
                Log.d("Home", "Current = $currentUserUid")

                if (otherUserId != null) {
                    // Kiểm tra nếu đã có trong cache
                    if (userCache.containsKey(otherUserId)) {
                        binding.userName.text = userCache[otherUserId]?.displayName ?: "Unknown"
                    } else {
                        // Hủy observer cũ nếu có
                        observers[position]?.let { observer ->
                            userViewModel.searchUserbyUidResult.removeObserver(observer)
                        }

                        // Tạo observer mới với tag position để có thể quản lý
                        val observer = Observer<SearchUserResult> { result ->
                            if (result.success && result.user?.uid == otherUserId) {
                                userCache[otherUserId] = result.user
                                binding.userName.text = result.user.displayName ?: "Unknown"
                            }
                        }

                        // Lưu observer mới
                        observers[position] = observer

                        // Đăng ký observer
                        (binding.root.context as? LifecycleOwner)?.let { lifecycleOwner ->
                            userViewModel.searchUserbyUidResult.observe(lifecycleOwner, observer)
                        }

                        // Tìm kiếm người dùng
                        userViewModel.searchUserbyUid(otherUserId)
                    }
                } else {
                    binding.userName.text = "Unknown"
                }
            }
            binding.messageText.text = conversation.lastMessage ?: ""
            binding.messageTime.text = formatTime(conversation.lastSendTime)

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
        holder.bind(conversations[position], position)
    }

    override fun onViewRecycled(holder: ConversationViewHolder) {
        super.onViewRecycled(holder)
        // Xóa observer khi view bị tái chế
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            observers[position]?.let { observer ->
                (holder.binding.root.context as? LifecycleOwner)?.let { lifecycleOwner ->
                    userViewModel.searchUserbyUidResult.removeObserver(observer)
                }
            }
            observers.remove(position)
        }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateData(newConversations: List<Conversation>) {
        // Xóa tất cả observers khi cập nhật dữ liệu mới
        clearAllObservers()
        conversations = newConversations
        notifyDataSetChanged()
    }

    fun updateCurrentUserUid(newUid: String) {
        if (currentUserUid != newUid) {
            currentUserUid = newUid
            // Xóa cache khi thay đổi người dùng hiện tại
            userCache.clear()
            if (conversations.isNotEmpty()) {
                notifyDataSetChanged()
            }
        }
    }

    private fun clearAllObservers() {
        for ((_, observer) in observers) {
            userViewModel.searchUserbyUidResult.removeObserver(observer)
        }
        observers.clear()
    }

    private fun formatTime(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(millis)

        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)

        val inputCal = Calendar.getInstance()
        inputCal.time = Date(millis)
        inputCal.set(Calendar.HOUR_OF_DAY, 0)
        inputCal.set(Calendar.MINUTE, 0)
        inputCal.set(Calendar.SECOND, 0)
        inputCal.set(Calendar.MILLISECOND, 0)

        val isToday = todayCal.timeInMillis == inputCal.timeInMillis

        val sdf = if (isToday) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM", Locale.getDefault())
        }

        return sdf.format(Date(millis))
    }
}