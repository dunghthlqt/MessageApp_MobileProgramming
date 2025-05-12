package com.demo.messageapp.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.messageapp.R
import com.demo.messageapp.databinding.FragmentChatBinding
import com.demo.messageapp.view.adapter.MessageAdapter
import com.demo.messageapp.viewmodel.MessageViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MessageAdapter
    private lateinit var messageViewModel: MessageViewModel
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

        adapter = MessageAdapter(emptyList())

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

        messageViewModel.addMessageListenerResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.success) {
                result.messageList?.let {
                    if (it.isEmpty()) {
                        binding.messageRecyclerView.visibility = View.GONE
                    } else {
                        binding.messageRecyclerView.visibility = View.VISIBLE

                        adapter.updateData(result.messageList)
                        scrollToBottom()
                    }
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
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
            val content: String = binding.editTextMessage.text.toString()

            messageViewModel.sendMessage(conversationId!!, userUid!!, content)
        }

        binding.btnBack.setOnClickListener {
            messageViewModel.removeMessageListener()
            findNavController().navigate(R.id.action_chatFragment_to_homeFragment)
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
        _binding = null
    }
}