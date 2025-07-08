package com.myapp.jikimi.presentation.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.User
import com.myapp.jikimi.databinding.FragmentProfileEditBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.presentation.utils.CustomDialogUtil
import com.myapp.jikimi.presentation.utils.showToast
import com.myapp.jikimi.viewmodel.AuthViewModel
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

        (activity as? MainActivity)?.hideBottomNavigation()

        // 로그인 상태 확인
        if (!viewModel.isLoggedIn()) {
            requireContext().showToast("로그인이 필요합니다")
            findNavController().navigate(R.id.loginFragment)
            return
        }

        setupObservers()
        setupListeners()
        loadUserProfile()
        setupBackButton()
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
                    with(binding) {
                        when (resource) {
                            is Resource.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                progressBar.visibility = View.GONE
                                currentUser = resource.data
                                currentUser?.let { user ->
                                    nicknameEt.setText(user.nickname)
                                    if (user.profileImageUrl.isNotEmpty()) {
                                        Glide.with(requireContext())
                                            .load(user.profileImageUrl)
                                            .placeholder(R.drawable.ic_launcher_foreground)
                                            .error(R.drawable.ic_launcher_foreground)
                                            .circleCrop()
                                            .into(profileCircleIv)
                                    }
                                }
                            }

                            is Resource.Error -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast(resource.message ?: "프로필을 불러오는데 실패했습니다")
                            }

                            null -> {
                                // 초기 상태
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        // LiveData observe에서 StateFlow collect로 변경
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateProfileStatus.collect { resource ->
                    with(binding) {
                        when (resource) {
                            is Resource.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                progressBar.visibility = View.GONE
                                resource.data?.let {
                                    requireContext().showToast("프로필이 수정되었습니다")
                                    findNavController().navigateUp()
                                }
                            }

                            is Resource.Error -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast(resource.message ?: "프로필 수정에 실패했습니다")
                            }

                            null -> {
                                // 초기 상태
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        // 로그아웃 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutStatus.collect { resource ->
                    with(binding) {
                        when (resource) {
                            is Resource.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                progressBar.visibility = View.GONE
                                // 로그아웃 성공 시 즉시 BottomNavigationView 숨기기
                                (activity as MainActivity).hideBottomNavigation()
                                requireContext().showToast("로그아웃 되었습니다")
                                findNavController().navigate(R.id.loginFragment)
                            }

                            is Resource.Error -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast(resource.message ?: "로그아웃에 실패했습니다")
                            }

                            null -> {
                                // 초기 상태
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        // 회원탈퇴 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteAccountStatus.collect { resource ->
                    with(binding) {
                        when (resource) {
                            is Resource.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                progressBar.visibility = View.GONE
                                // 회원탈퇴 성공 시 즉시 BottomNavigationView 숨기기
                                (activity as MainActivity).hideBottomNavigation()
                                requireContext().showToast("회원탈퇴가 완료되었습니다")
                                findNavController().navigate(R.id.registerFragment)
                            }

                            is Resource.Error -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast(resource.message ?: "회원탈퇴에 실패했습니다")
                            }

                            null -> {
                                // 초기 상태
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            profileCircleIv.setOnClickListener {
                openGallery()
            }
            changeProfileBtn.setOnClickListener {
                openGallery()
            }
            saveBtn.setOnClickListener {
                val nickname = nicknameEt.text.toString().trim()

                if (validateInputs(nickname)) {
                    Log.d("ProfileEditFragment", "프로필 업데이트 - 닉네임: $nickname, 이미지 URI: $imageUri")
                    viewModel.updateProfile(nickname, imageUri)
                }
            }

            // tvMenuLeft 클릭 시 로그아웃
            menuLeftTv.setOnClickListener {
                showLogoutConfirmDialog()
            }

            // tvMenuRight 클릭 시 회원탈퇴
            menuRightTv.setOnClickListener {
                showDeleteAccountConfirmDialog()
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        CustomDialogUtil.showDialog(
            context = requireContext(),
            message = "로그아웃 하시겠습니까?",
            positiveText = "로그아웃",
            onConfirm = {
                viewModel.logout()
            }
        )
    }

    private fun showDeleteAccountConfirmDialog() {
        CustomDialogUtil.showDialog(
            context = requireContext(),
            message = "정말 탈퇴하시겠습니까?",
            positiveText = "탈퇴",
            onConfirm = {
                viewModel.deleteAccount()
                findNavController().navigate(R.id.registerFragment)
            }
        )
    }

    private fun validateInputs(nickname: String): Boolean {
        if (nickname.isEmpty()) {
            binding.nicknameEt.error = "닉네임을 입력하세요"
            return false
        }
        binding.nicknameTil.error = null
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
                .into(binding.profileCircleIv)
        }
    }

    private fun setupBackButton() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity)?.showBottomNavigation()
    }
}