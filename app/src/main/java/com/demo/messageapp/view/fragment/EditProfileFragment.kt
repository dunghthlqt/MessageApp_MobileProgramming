package com.demo.messageapp.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.demo.messageapp.R
import com.demo.messageapp.databinding.FragmentEditProfileBinding
import com.demo.messageapp.viewmodel.AuthViewModel
import com.demo.messageapp.viewmodel.UserViewModel

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    private val PICK_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_settingsFragment)
        }

        // Lấy thông tin người dùng hiện tại
        authViewModel.currentUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                userViewModel.searchUserbyUid(firebaseUser.uid)
                userViewModel.searchUserbyUidResult.observe(viewLifecycleOwner) { result ->
                    if (result.success && result.user != null) {
                        binding.editTextDisplayName.setText(result.user.displayName)
                        if (result.user.avatarUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(result.user.avatarUrl)
                                .into(binding.avatarImageView)
                        }
                    }
                }
            }
        }

        // Chọn ảnh đại diện
        binding.btnChangeAvatar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1002
                )
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
        }

        // Lưu thông tin
        binding.btnSaveProfile.setOnClickListener {
            val newDisplayName = binding.editTextDisplayName.text.toString().trim()
            if (newDisplayName.isEmpty()) {
                Toast.makeText(requireContext(), "Display name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.updateUserProfile(newDisplayName, selectedImageUri, requireContext()) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } else {
            Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            binding.avatarImageView.setImageURI(selectedImageUri)
        }
    }
}