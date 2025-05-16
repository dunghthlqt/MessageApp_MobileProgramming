package com.demo.messageapp.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.demo.messageapp.R
import com.demo.messageapp.model.Conversation

class ConversationOptionsDialog(
    private val context: Context,
    private val onDeleteListener: () -> Unit
) {
    private val dialog: Dialog = Dialog(context)

    init {
        setupDialog()
    }

    private fun setupDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.conversation_options)

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)

        dialog.findViewById<TextView>(R.id.textViewDelete).setOnClickListener {
            onDeleteListener()
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.show()
    }
}