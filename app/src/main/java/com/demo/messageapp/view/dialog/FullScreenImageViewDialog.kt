package com.demo.messageapp.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.demo.messageapp.R
import com.demo.messageapp.databinding.DialogFullscreenImageBinding

class FullScreenImageViewDialog(
    private val context: Context,
    private val imageUrl: String
) {
    private val dialog: Dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    private lateinit var binding: DialogFullscreenImageBinding

    init {
        setupDialog()
    }

    private fun setupDialog() {
        // Sử dụng View Binding
        binding = DialogFullscreenImageBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        // Thiết lập window attributes
        dialog.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.BLACK))
        }

        // Tải hình ảnh bằng Glide
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.fullscreenImageView)

        // Thiết lập action listeners
        setupActionListeners()
    }

    private fun setupActionListeners() {
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}