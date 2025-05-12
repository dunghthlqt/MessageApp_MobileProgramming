package com.demo.messageapp.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.demo.messageapp.R
import com.demo.messageapp.databinding.FragmentNewContactBinding
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.UserViewModel

class NewContactFragment : DialogFragment() {

    private var _binding: FragmentNewContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    private var currentUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        authViewModel.currentUser.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                currentUid = result.uid
                userViewModel.getContactsUIDList(it.uid)
            }
        })

        authViewModel.getCurrentUser()

        userViewModel.searchUserbyEmailResult.observe(viewLifecycleOwner, Observer { result ->
            if(result.success) {
                
            } else {
                Log.d("ContactsFragment", "Error = ${result.errorMessage}")
            }
        })

        binding.btnAdd.setOnClickListener {
            val email = binding.etName.text.toString().trim()
            if (email.isNotEmpty()) {
                userViewModel.searchUserbyEmail(email)
            } else {
                binding.tilName.error = "Email không được để trống"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}