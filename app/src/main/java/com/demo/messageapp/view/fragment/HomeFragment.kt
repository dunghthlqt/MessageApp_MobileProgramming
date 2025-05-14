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
import com.demo.messageapp.databinding.FragmentHomeBinding
import com.demo.messageapp.model.Conversation
import com.demo.messageapp.view.adapter.ConversationAdapter
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.ConversationViewModel
import com.demo.messageapp.viewmodel.UserViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    private var currentUid: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        clearBackStack()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        conversationViewModel = ViewModelProvider(this)[ConversationViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Khởi tạo adapter trước với danh sách rỗng
        adapter = ConversationAdapter(
            conversations = emptyList(),
            currentUserUid = currentUid,  // Sẽ được cập nhật sau khi có currentUser
            userViewModel = userViewModel,
            onConversationClick = { conversation ->
                navigateToChatFragment(conversation)
            }
        )

        // Thiết lập adapter cho RecyclerView
        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.conversationRecyclerView.adapter = adapter

        authViewModel.currentUser.observe(viewLifecycleOwner, Observer { result ->
            if (result != null) {
                currentUid = result.uid
                // Cập nhật currentUserUid cho adapter
                adapter.updateCurrentUserUid(currentUid)
                conversationViewModel.getConversationList(currentUid)
            }
        })

        authViewModel.getCurrentUser()
        
        conversationViewModel.getConversationListResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.success) {
                result.conversationList?.let {
                    if (it.isEmpty()) {
                        binding.conversationRecyclerView.visibility = View.GONE
                        binding.noMessagesTextView.visibility = View.VISIBLE
                    } else {
                        binding.conversationRecyclerView.visibility = View.VISIBLE
                        binding.noMessagesTextView.visibility = View.GONE

                        adapter.updateData(result.conversationList)
                    }
                }
            } else {
                Log.d("Home", "Error = ${result.errorMessage}")
            }
        })

        binding.btnSearch.setOnClickListener{

        }
        binding.btnContacts.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_contactsFragment)
        }
        binding.btnSetting.setOnClickListener{

        }

        binding.btnAdd.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_addMessageFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        clearBackStack()
    }

    private fun clearBackStack() {
        val navController = findNavController()
        navController.popBackStack(R.id.homeFragment, false)
    }

    private fun navigateToChatFragment(conversation: Conversation) {
         val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(
             conversationId = conversation.id,
             conversationName = conversation.conversationName,
             userUid = currentUid
         )
         findNavController().navigate(action)
    }
}