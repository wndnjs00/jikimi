package com.myapp.jikimi.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.databinding.FragmentRegisterBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.presentation.utils.showToast
import com.myapp.jikimi.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomNavigation()

        setupObservers()
        setupListeners()
    }

    // viewModel에서 StateFlow로 회원가입 상태를 관찰
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signupStatus.collect { resource ->
                    with(binding) {
                        when (resource) {
                            is Resource.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast("회원가입 성공")
                                findNavController().navigate(R.id.loginFragment)
                            }

                            is Resource.Error -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast(resource.message ?: "회원가입 실패")
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
            registerBtn.setOnClickListener {
                val email = emailEt.text.toString().trim()
                val nickname = nicknameEt.text.toString().trim()
                val password = passwordEt.text.toString().trim()
                val confirmPassword = passwordConfirmEt.text.toString().trim()
                val termsChecked = checkboxTerms.isChecked
                val locationTermsChecked = checkboxTerms2.isChecked

                if (validateInputs(
                        email,
                        nickname,
                        password,
                        confirmPassword,
                        termsChecked,
                        locationTermsChecked
                    )
                ) {
                    viewModel.signup(email, password, nickname)
                }
            }

            loginTv.setOnClickListener {
                findNavController().navigate(R.id.loginFragment)
            }
            arrowIv.setOnClickListener {
                openPdf("secret.pdf")
            }
            arrow2Iv.setOnClickListener {
                openPdf("map.pdf")
            }
        }
    }


    private fun validateInputs(
        email: String,
        nickname: String,
        password: String,
        confirmPassword: String,
        termsChecked: Boolean,
        locationTermsChecked: Boolean
    ): Boolean {
        var isValid = true

        with(binding) {
            if (email.isEmpty()) {
                emailTil.error = "이메일을 입력하세요"
                isValid = false
            } else {
                emailTil.error = null
            }

            if (nickname.isEmpty()) {
                nicknameTil.error = "닉네임을 입력하세요"
                isValid = false
            } else {
                nicknameTil.error = null
            }

            if (password.isEmpty()) {
                passwordTil.error = "비밀번호를 입력하세요"
                isValid = false
            } else {
                passwordTil.error = null
            }

            if (confirmPassword.isEmpty()) {
                passwordConfirmTil.error = "비밀번호 확인을 입력하세요"
                isValid = false
            } else if (password != confirmPassword) {
                passwordConfirmTil.error = "비밀번호가 일치하지 않습니다"
                isValid = false
            } else {
                passwordConfirmTil.error = null
            }

            // 이용약관 동의 체크 확인
            if (!termsChecked) {
                requireContext().showToast("이용약관에 동의해주세요")
                isValid = false
            }

            // 위치서비스 이용약관 동의 체크 확인
            if (!locationTermsChecked) {
                requireContext().showToast("위치서비스 이용약관에 동의해주세요")
                isValid = false
            }
            return isValid
        }
    }

    private fun openPdf(fileName: String) {
        try {
            // assets 디렉토리에서 filesDir 디렉토리로 복사
            val inputStream = requireContext().assets.open(fileName)
            val outputFile = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // FileProvider를 통해 Uri 생성
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                outputFile
            )
            // Intent를 통해 PDF 열기
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            requireContext().showToast("PDF를 열 수 없습니다: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}