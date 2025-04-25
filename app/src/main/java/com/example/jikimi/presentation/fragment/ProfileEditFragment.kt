package com.example.jikimi.presentation.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.jikimi.R
import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.User
import com.example.jikimi.databinding.CustomDialogBinding
import com.example.jikimi.databinding.FragmentProfileEditBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()

        // 로그인 상태 확인
        if (!viewModel.isLoggedIn()) {
            (activity as MainActivity).showToast("로그인이 필요합니다")
            findNavController().navigate(R.id.loginFragment)
            return
        }

        setupObservers()
        setupListeners()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = viewModel.getCurrentUser()?.uid ?: return
        viewModel.getUserProfile(userId)
    }

    private fun setupObservers() {
        // LiveData observe에서 StateFlow collect로 변경
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userProfile.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            currentUser = resource.data
                            currentUser?.let { user ->
                                binding.etNickname.setText(user.nickname)
                                if (user.profileImageUrl.isNotEmpty()) {
                                    Glide.with(requireContext())
                                        .load(user.profileImageUrl)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .circleCrop()
                                        .into(binding.cardProfileImage)
                                }
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            (activity as MainActivity).showToast(resource.message ?: "프로필을 불러오는데 실패했습니다")
                        }
                    }
                }
            }
        }

        // LiveData observe에서 StateFlow collect로 변경
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateProfileStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
//                            binding.btnSave.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            resource.data?.let {
                                (activity as MainActivity).showToast("프로필이 수정되었습니다")
                                // CommnunityFragment의 프로필 정보 업데이트!!

                                findNavController().navigateUp()
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
//                            binding.btnSave.isEnabled = true
                            (activity as MainActivity).showToast(resource.message ?: "프로필 수정에 실패했습니다")
                        }
                    }
                }
            }
        }

        // 로그아웃 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            // 로그아웃 성공 시 즉시 BottomNavigationView 숨기기
                            (activity as MainActivity).hideBottomNavigation()
                            (activity as MainActivity).showToast("로그아웃 되었습니다")
                            findNavController().navigate(R.id.loginFragment)
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            (activity as MainActivity).showToast(resource.message ?: "로그아웃에 실패했습니다")
                        }
                    }
                }
            }
        }

        // 회원탈퇴 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteAccountStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            // 회원탈퇴 성공 시 즉시 BottomNavigationView 숨기기
                            (activity as MainActivity).hideBottomNavigation()
                            (activity as MainActivity).showToast("회원탈퇴가 완료되었습니다")
                            findNavController().navigate(R.id.registerFragment)
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            (activity as MainActivity).showToast(resource.message ?: "회원탈퇴에 실패했습니다")
                        }
                    }
                }
            }
        }

    }



    private fun setupListeners() {
        binding.cardProfileImage.setOnClickListener {
            openGallery()
        }

        binding.btnChangeProfile.setOnClickListener {
            openGallery()
        }

        binding.btnSave.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()

            if (validateInputs(nickname)) {
                Log.d("ProfileEditFragment", "프로필 업데이트 - 닉네임: $nickname, 이미지 URI: $imageUri")
                viewModel.updateProfile(nickname, imageUri)
            }
        }

        // tvMenuLeft 클릭 시 로그아웃
        binding.tvMenuLeft.setOnClickListener {
            showLogoutConfirmDialog()
        }

        // tvMenuRight 클릭 시 회원탈퇴
        binding.tvMenuRight.setOnClickListener {
            showDeleteAccountConfirmDialog()
        }
    }

    private fun showLogoutConfirmDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 다이얼로그 텍스트 설정
        dialogBinding.dialogTv.text = "로그아웃 하시겠습니까?"
        // 버튼 텍스트 변경
        dialogBinding.dialogDeleteBtn.text = "로그아웃"

        dialogBinding.dialogCancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.dialogDeleteBtn.setOnClickListener {
            viewModel.logout()
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun showDeleteAccountConfirmDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 텍스트 설정
        dialogBinding.dialogTv.text = "정말 탈퇴하시겠습니까?"
        // 버튼 텍스트 변경
        dialogBinding.dialogDeleteBtn.text = "탈퇴"

        dialogBinding.dialogCancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.dialogDeleteBtn.setOnClickListener {
            viewModel.deleteAccount()
            findNavController().navigate(R.id.registerFragment)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun validateInputs(nickname: String): Boolean {
        if (nickname.isEmpty()) {
            binding.etNickname.error = "닉네임을 입력하세요"
            return false
        }
        binding.tilNickname.error = null
        return true
    }

    private fun openGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            // 디버그 로그 추가
            Log.d("ProfileEditFragment", "선택된 이미지 URI: $imageUri")

            // 이미지 로드 및 표시
            Glide.with(requireContext())
                .load(imageUri)
                .placeholder(R.drawable.jikimi_img)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(binding.cardProfileImage)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        (activity as MainActivity)?.showBottomNavigation()
    }
}