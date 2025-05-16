package com.demo.messageapp.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.demo.messageapp.utils.navigateToHomeAndClearBackStack
import com.demo.messageapp.databinding.FragmentChatBinding
import com.demo.messageapp.view.adapter.MessageAdapter
import com.demo.messageapp.view.dialog.ConversationOptionsDialog
import com.demo.messageapp.viewmodel.ConversationViewModel
import com.demo.messageapp.viewmodel.MessageViewModel
import com.demo.messageapp.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MessageAdapter
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var layoutManager: LinearLayoutManager

    private var conversationId: String? = null
    private var conversationName: String? = null
    private var userUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageViewModel = ViewModelProvider(requireActivity())[MessageViewModel::class.java]
        conversationViewModel = ViewModelProvider(this)[ConversationViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        adapter = MessageAdapter(
            messages = emptyList(),
            context = requireContext(),
            userUid = userUid ?: "",
            onReplyListener = { message ->
                // TODO: Xử lý reply, ví dụ: mở giao diện trả lời
                Toast.makeText(requireContext(), "Reply to: ${message.content}", Toast.LENGTH_SHORT).show()
            },
            onDeleteListener = { message ->
                messageViewModel.deleteMessage(conversationId!!, message.id)
            },
            onReactionAddedListener = { message, emoji ->
                messageViewModel.addReaction(conversationId!!, message.id, emoji, userUid!!)
            }
        )

        // Thay đổi: lưu layoutManager vào biến để sử dụng sau này
        layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Đảm bảo tin nhắn mới sẽ ở dưới cùng

        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.messageRecyclerView.adapter = adapter

        // Thêm: Lắng nghe sự thay đổi kích thước (khi bàn phím xuất hiện)
        binding.messageRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.messageRecyclerView.post {
                    scrollToBottom()
                }
            }
        }

        arguments?.let {
            conversationId = it.getString("conversationId")
            conversationName = it.getString("conversationName")
            userUid = it.getString("userUid")

            binding.textViewReceiverName.text = conversationName
            messageViewModel.addMessageListener(conversationId!!, userUid!!)
        }

        conversationViewModel.searchConversationByUid(conversationId!!)

        conversationViewModel.searchConversationByUidResult.observe(viewLifecycleOwner, Observer { result ->
            if(result.success) {
                val otherUserId = result.conversation?.participantIds?.firstOrNull { it != userUid }
                if (otherUserId != null) {
                    userViewModel.searchUserbyUid(otherUserId)
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        userViewModel.searchUserbyUidResult.observe(viewLifecycleOwner, Observer { result ->
            if(result.success) {
                if(result.user != null) {
                    context?.let {
                        Glide.with(it)
                            .load(result.user.avatarUrl)
                            .into(binding.imageViewProfilePic)
                    }
                    binding.textViewReceiverName.text = result.user.displayName
                    if(result.user.isOnline) {
                        if(result.user.lastSeen < System.currentTimeMillis() && result.user.lastSeen > (System.currentTimeMillis() - 180000)) {
                            binding.textViewStatus.text = "Online"
                        } else {
                            binding.textViewStatus.text = formatTime(result.user.lastSeen)
                        }
                    } else {
                        binding.textViewStatus.text = formatTime(result.user.lastSeen)
                    }
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        messageViewModel.addMessageListenerResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.success) {
                result.messageList?.let {
                    if (it.isEmpty()) {
                        binding.messageRecyclerView.visibility = View.GONE
                    } else {
                        binding.messageRecyclerView.visibility = View.VISIBLE

                        adapter.updateData(result.messageList)
                        if (isNearBottom()) {
                            scrollToBottom()
                        }
                    }
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        messageViewModel.addReactionResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.first) {
                Toast.makeText(requireContext(), "Reaction added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error: ${result.second}", Toast.LENGTH_SHORT).show()
            }
        })

        messageViewModel.sendMessageResult.observe(viewLifecycleOwner, Observer { result ->
            if(result.first) {
                binding.editTextMessage.setText("")
            }
        })

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnSend.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
            }
        })

        binding.btnSend.setOnClickListener {
            val content: String = binding.editTextMessage.text.toString().trim()
            messageViewModel.sendMessage(conversationId!!, userUid!!, content)
        }

        binding.btnBack.setOnClickListener {
            messageViewModel.removeMessageListener()
            findNavController().navigateToHomeAndClearBackStack()
        }

        binding.btnMore.setOnClickListener {
            val dialog = ConversationOptionsDialog(
                context = requireContext(),
                onDeleteListener = {
                    conversationViewModel.deleteConversation(conversationId!!)
                }
            )
            dialog.show()
        }
    }

    // Thêm: Phương thức để cuộn xuống tin nhắn cuối cùng
    private fun scrollToBottom() {
        if (adapter.itemCount > 0) {
            binding.messageRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    // Thêm: Phương thức để kiểm tra xem người dùng có đang ở gần cuối danh sách không
    private fun isNearBottom(): Boolean {
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        return lastVisibleItemPosition >= adapter.itemCount - 2
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageViewModel.removeMessageListener()
        _binding = null
    }

    private fun formatTime(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(millis)

        // Lấy ngày hiện tại (00:00:00.000)
        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)

        // Lấy ngày của thời điểm đầu vào (00:00:00.000)
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
            SimpleDateFormat("dd MM 'at' HH:mm", Locale.getDefault())
        }

        val formattedTime = sdf.format(Date(millis))
        return "last sent ${if (isToday) "at " else ""}$formattedTime"
    }
}