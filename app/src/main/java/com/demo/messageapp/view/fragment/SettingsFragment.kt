package com.demo.messageapp.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.demo.messageapp.R
import com.demo.messageapp.databinding.FragmentSettingsBinding
import com.demo.messageapp.view.HomeActivity
import com.demo.messageapp.view.WelcomeActivity
import com.demo.messageapp.viewmodel.AuthViewModel

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_editProfileFragment)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnChats.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
        }
        binding.btnContacts.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_contactsFragment)
        }

        binding.btnSetting.isEnabled = false
        binding.btnSetting.alpha = 0.5f
    }
}