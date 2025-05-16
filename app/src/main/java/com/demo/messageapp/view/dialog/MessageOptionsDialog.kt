package com.demo.messageapp.view.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.messageapp.R
import com.demo.messageapp.model.Message
import com.demo.messageapp.view.adapter.EmojiAdapter

class MessageOptionsDialog(
    private val context: Context,
    private val message: Message,
    private val onReplyListener: (Message) -> Unit,
    private val onDeleteListener: (Message) -> Unit,
    private val onReactionAddedListener: (Message, String) -> Unit
) {
    private val dialog: Dialog = Dialog(context)

    init {
        setupDialog()
    }

    private fun setupDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.message_options)

        // Thiết lập vị trí và kích thước của dialog
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)

        // Thiết lập RecyclerView cho emoji
        setupEmojiRecyclerView()

        // Thiết lập các action cho các tùy chọn
        setupActionListeners()
    }

    private fun setupEmojiRecyclerView() {
        val recyclerViewEmojis = dialog.findViewById<RecyclerView>(R.id.recyclerViewEmojis)

        // Danh sách emoji phổ biến
        recyclerViewEmojis.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val emojiList = listOf("👍", "❤️", "😂", "😮", "😢", "😠")

        val adapter = EmojiAdapter(emojiList) { emoji ->
            onReactionAddedListener(message, emoji)
            dialog.dismiss()
        }

        recyclerViewEmojis.adapter = adapter
    }

    private fun setupActionListeners() {
        // Reply action
        dialog.findViewById<TextView>(R.id.textViewReply).setOnClickListener {
            onReplyListener(message)
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.textViewCopy).setOnClickListener {
            copyMessageToClipboard()
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.textViewDelete).setOnClickListener {
            onDeleteListener(message)
            dialog.dismiss()
        }
    }

    private fun copyMessageToClipboard() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Message", message.content)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun show() {
        dialog.show()
    }
}