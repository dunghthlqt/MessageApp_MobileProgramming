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
import com.demo.messageapp.utils.navigateToHomeAndClearBackStack
import com.demo.messageapp.databinding.FragmentContactsBinding
import com.demo.messageapp.view.adapter.ContactsAdapter
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.ConversationViewModel
import com.demo.messageapp.viewmodel.UserViewModel

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ContactsAdapter
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var conversationViewModel: ConversationViewModel
    private var currentUid: String = ""
    private var conversationName: String = ""
    private var participantIds: List<String> = emptyList()

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
        conversationViewModel = ViewModelProvider(requireActivity())[ConversationViewModel::class.java]

        conversationViewModel.resetConversationTwoUidResult()

        adapter = ContactsAdapter(emptyList(), context = requireContext()) { contact ->
            conversationName = ""
            conversationName = contact.displayName
            participantIds = listOf(currentUid, contact.uid)
            conversationViewModel.getConversationTwoUID(currentUid, contact.uid)
        }

        binding.conversationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.conversationRecyclerView.adapter = adapter

        conversationViewModel.createConversationResult.observe(viewLifecycleOwner, Observer { result ->
            if(result.success) {
                navigateToChatFragment(result.conversationId!!)
            } else {
                Log.d("ContactsFragment", "Error = ${result.errorMessage}")
            }
        })

        conversationViewModel.getConversationTwoUidResult.observe(viewLifecycleOwner, Observer { result ->
            if(result.success) {
                if(result != null && !result.conversationId.equals("")) {
                    navigateToChatFragment(result.conversationId!!)
                } else {
                    conversationViewModel.createConversation(participantIds, "", currentUid)
                }
            } else {
                Log.d("ContactsFragment", "Error = ${result.errorMessage}")
            }
        })

        userViewModel.getUserListResult.observe(viewLifecycleOwner, Observer { result ->
            if (result.success) {
                result.userList?.let {
                    adapter.updateData(it)
                }
            } else {
                Log.d("ContactsFragment", "Error = ${result.errorMessage}")
            }
        })

        userViewModel.addContactListenerResult.observe(viewLifecycleOwner, Observer { result ->
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
                currentUid = result.uid
                userViewModel.addContactListener(currentUid)
            }
        })

        authViewModel.getCurrentUser()

        binding.btnChat.setOnClickListener {
            findNavController().navigateToHomeAndClearBackStack()
        }

        binding.btnSetting.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_settingsFragment)
        }

        binding.btnAdd.setOnClickListener {
            val dialog = NewContactFragment()
            dialog.show(parentFragmentManager, "NewContactDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        userViewModel.removeContactListener()
    }

    override fun onStop() {
        super.onStop()
        conversationViewModel.resetConversationTwoUidResult()
    }

    private fun navigateToChatFragment(conversationId: String) {
        val action = ContactsFragmentDirections.actionContactsFragmentToChatFragment(
            conversationId = conversationId,
            conversationName = conversationName,
            userUid = currentUid
        )
        findNavController().navigate(action)
    }
}