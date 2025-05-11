package com.demo.messageapp.view.fragment

import android.os.Bundle
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
import com.demo.messageapp.databinding.FragmentContactsBinding
import com.demo.messageapp.view.adapter.ContactsAdapter
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.UserViewModel

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ContactsAdapter
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        adapter = ContactsAdapter(emptyList()) { contact ->
            // TODO: Xử lý khi người dùng nhấn vào 1 cuộc trò chuyện
        }

        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.conversationRecyclerView.adapter = adapter

        userViewModel.getUserListResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.success) {
                result.userList?.let {
                    for (user in it) {
                        Log.d("ContactsFragment", "User = ${user.displayName}")
                    }
                    adapter.updateData(it)
                }
            } else {
                Log.d("ContactsFragment", "Error = ${result.errorMessage}")
            }
        })

        userViewModel.getContactsUIDListResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.success) {
                result.contactsUIDList?.let {
                    if (it.isEmpty()) {
                        binding.conversationRecyclerView.visibility = View.GONE
                        binding.noMessagesTextView.visibility = View.VISIBLE
                    } else {
                        binding.conversationRecyclerView.visibility = View.VISIBLE
                        binding.noMessagesTextView.visibility = View.GONE
                        userViewModel.getUserList(it)
                    }
                }
            } else {
                Log.d("ContactsFragment", "Error = ${result.errorMessage}")
            }
        })

        authViewModel.currentUser.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                userViewModel.getContactsUIDList(it.uid)
            }
        })

        authViewModel.getCurrentUser()

        binding.btnChat.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}