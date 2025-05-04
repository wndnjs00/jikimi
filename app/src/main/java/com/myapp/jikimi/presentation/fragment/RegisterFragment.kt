package com.myapp.jikimi.presentation.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.databinding.FragmentRegisterBinding
import com.myapp.jikimi.presentation.activity.MainActivity
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

        // BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()

        setupObservers()
        setupListeners()
    }


    // viewModel에서 StateFlow로 회원가입 상태를 관찰
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signupStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
//                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            (activity as MainActivity).showToast("회원가입 성공")

                            findNavController().navigate(R.id.loginFragment)
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.VISIBLE
                            (activity as MainActivity).showToast(resource.message ?: "회원가입 실패")
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val nickname = binding.etNickname.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val termsChecked = binding.checkboxTerms.isChecked
            val locationTermsChecked = binding.checkboxTerms2.isChecked

            if (validateInputs(email, nickname, password, confirmPassword, termsChecked, locationTermsChecked)) {
                viewModel.signup(email, password, nickname)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        // ivArrow 클릭 시 secret.pdf 열기
        binding.ivArrow.setOnClickListener {
            openPdf("secret.pdf")
        }

        // ivArrow2 클릭 시 map.pdf 열기
        binding.ivArrow2.setOnClickListener {
            openPdf("map.pdf")
        }
    }


    private fun validateInputs(email: String,
                               nickname: String,
                               password: String,
                               confirmPassword: String,
                               termsChecked: Boolean,
                               locationTermsChecked: Boolean
    ): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "이메일을 입력하세요"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (nickname.isEmpty()) {
            binding.tilNickname.error = "닉네임을 입력하세요"
            isValid = false
        } else {
            binding.tilNickname.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "비밀번호를 입력하세요"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "비밀번호 확인을 입력하세요"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "비밀번호가 일치하지 않습니다"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        // 이용약관 동의 체크 확인
        if (!termsChecked) {
            (activity as MainActivity).showToast("이용약관에 동의해주세요")
            isValid = false
        }

        // 위치서비스 이용약관 동의 체크 확인
        if (!locationTermsChecked) {
            (activity as MainActivity).showToast("위치서비스 이용약관에 동의해주세요")
            isValid = false
        }

        return isValid
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
            (activity as MainActivity).showToast("PDF를 열 수 없습니다: ${e.message}")
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}