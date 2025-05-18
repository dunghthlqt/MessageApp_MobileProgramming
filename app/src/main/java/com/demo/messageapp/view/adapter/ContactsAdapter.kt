package com.demo.messageapp.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.messageapp.databinding.ItemContactBinding
import com.demo.messageapp.model.User

class ContactsAdapter(
    private var contacts: List<User>,
    private val context: Context,
    private val onContactsClick: (User) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    inner class ContactsViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: User) {
            val name = contact.displayName
            val isOnline = if (contact.isOnline) "Online" else "Offline"

            Glide.with(context)
                .load(contact.avatarUrl)
                .into(binding.avatar)

            binding.userName.text = name
            binding.isOnline.text = isOnline

            binding.root.setOnClickListener {
                onContactsClick(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(newContacts: List<User>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}