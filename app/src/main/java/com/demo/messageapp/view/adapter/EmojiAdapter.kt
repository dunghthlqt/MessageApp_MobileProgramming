package com.demo.messageapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.messageapp.R

class EmojiAdapter(
    private val emojiList: List<String>,
    private val onEmojiClickListener: (String) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewEmoji: TextView = itemView.findViewById(R.id.textViewEmoji)

        init {
            itemView.setOnClickListener {
                onEmojiClickListener(emojiList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emoji, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.textViewEmoji.text = emojiList[position]
    }

    override fun getItemCount() = emojiList.size
}