package com.demo.messageapp.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.demo.messageapp.databinding.FragmentChatBinding
import com.demo.messageapp.model.ReplyInfo
import com.demo.messageapp.utils.navigateToHomeAndClearBackStack
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
    private var userName: String? = null
    private var anotherUid: String = ""
    private var anotherName: String? = null
    private var originalMessageId: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Cần quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uploadImageToCloudinary(it) }
    }

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

        arguments?.let {
            conversationId = it.getString("conversationId")
            conversationName = it.getString("conversationName")
            userUid = it.getString("userUid")

            userUid?.let {
                userViewModel.getUserNameByUid(it) { name ->
                    userName = name
                }
            }

            binding.textViewReceiverName.text = conversationName
            messageViewModel.addMessageListener(conversationId!!, userUid!!)
        }

        adapter = MessageAdapter(
            messages = emptyList(),
            context = requireContext(),
            userUid = userUid ?: "",
            userViewModel = userViewModel,
            onReplyListener = { message ->
                binding.replyPreview.replyPreviewText.text = message.content
                originalMessageId = message.id
                if(message.isSentByMe) {
                    binding.replyPreview.replySenderName.text = userName
                } else {
                    binding.replyPreview.replySenderName.text = anotherName
                }
                binding.replyPreview.root.visibility = View.VISIBLE
            },
            onDeleteListener = { message ->
                messageViewModel.deleteMessage(conversationId!!, message.id)
            },
            onReactionAddedListener = { message, emoji ->
                messageViewModel.addReaction(conversationId!!, message.id, emoji, userUid!!)
            },
            onOriginalMessageClickedListener = { originalMessageId ->
                val position = adapter.getMessages().indexOfFirst { it.id == originalMessageId }
                if (position != -1) {
                    binding.messageRecyclerView.smoothScrollToPosition(position)
                }
            }
        )

        layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.messageRecyclerView.adapter = adapter

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
                            .thumbnail(0.25f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontTransform()
                            .into(binding.imageViewProfilePic)
                    }
                    anotherUid = result.user.uid
                    anotherName = result.user.displayName
                    binding.textViewReceiverName.text = anotherName
                    if(result.user.isOnline) {
                        if(result.user.lastSeen < System.currentTimeMillis() && result.user.lastSeen > (System.currentTimeMillis() - 180000)) {
                            binding.textViewStatus.text = "Online"
                        } else {
                            binding.textViewStatus.text = formatTime(result.user.lastSeen)
                        }
                    } else {
                        binding.textViewStatus.text = formatTime(result.user.lastSeen)
                    }
                    binding.messageRecyclerView.visibility = View.VISIBLE
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
                        adapter.updateData(result.messageList)
                        scrollToBottom()
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
                binding.replyPreview.root.visibility = View.GONE
            }
        })

        // Setup Listeners
        binding.messageRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.messageRecyclerView.post {
                    scrollToBottom()
                }
            }
        }

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnSend.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                binding.btnAttachment.visibility = if (!s.isNullOrBlank()) View.GONE else View.VISIBLE
            }
        })

        binding.btnSend.setOnClickListener {
            val content: String = binding.editTextMessage.text.toString().trim()
            if(binding.replyPreview.root.visibility == View.VISIBLE) {
                val replyContent = binding.replyPreview.replyPreviewText.text.toString()
                if(binding.replyPreview.replySenderName.text.equals(anotherName)) {
                    val reply = ReplyInfo(originalMessageId, anotherUid, replyContent)
                    messageViewModel.sendReplyMessage(conversationId!!, userUid!!, content, "text", reply)
                } else {
                    val reply = userUid?.let { it1 ->
                        ReplyInfo(originalMessageId, it1, replyContent)
                    }
                    Log.d("Chat", "2")
                    Log.d("Chat", "$userUid")
                    if (reply != null) {
                        messageViewModel.sendReplyMessage(conversationId!!, userUid!!, content, "text", reply)
                    }
                }
            } else {
                messageViewModel.sendMessage(conversationId!!, userUid!!, content, "text")
            }
        }

        binding.btnBack.setOnClickListener {
            messageViewModel.removeMessageListener()
            findNavController().navigateToHomeAndClearBackStack()
        }

        binding.btnMore.setOnClickListener {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = location[0] + (view.width - 400)
            val y = location[1]
            val dialog = ConversationOptionsDialog(
                context = requireContext(),
                onDeleteListener = {
                    conversationViewModel.deleteConversation(conversationId!!)
                    messageViewModel.removeMessageListener()
                    findNavController().navigateToHomeAndClearBackStack()
                },
                x = x,
                y = y
            )
            dialog.show()
        }

        binding.replyPreview.btnCancelReply.setOnClickListener {
            binding.replyPreview.root.visibility = View.GONE
        }
        binding.btnAttachment.setOnClickListener {
            checkStoragePermission()
        }
    }

    private fun scrollToBottom() {
        if (adapter.itemCount > 0) {
            binding.messageRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

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

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Mở trình chọn ảnh
    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    // Tải ảnh lên Cloudinary
    private fun uploadImageToCloudinary(imageUri: Uri) {
        val filePath = getRealPathFromUri(imageUri)
        if (filePath != null) {
            MediaManager.get().upload(filePath)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Toast.makeText(requireContext(), "Đang tải ảnh...", Toast.LENGTH_SHORT).show()
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {

                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        var imageUrl = resultData["url"] as String
                        if (imageUrl.startsWith("http://")) {
                            imageUrl = imageUrl.replace("http://", "https://")
                        }
                        sendImageMessage(imageUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(requireContext(), "Lỗi: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {

                    }
                })
                .dispatch()
        } else {
            Toast.makeText(requireContext(), "Không thể lấy đường dẫn ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndexOrThrow("_data")
            it.getString(columnIndex)
        }
    }

    private fun sendImageMessage(imageUrl: String) {
        messageViewModel.sendMessage(conversationId!!, userUid!!, imageUrl, "image")
    }
}