package com.example.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jikimi.R
import com.example.jikimi.Resource
import com.example.jikimi.databinding.FragmentRegisterBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                            binding.progressBar.visibility = View.GONE
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

            if (validateInputs(email, nickname, password, confirmPassword)) {
                viewModel.signup(email, password, nickname)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }


    private fun validateInputs(email: String, nickname: String, password: String, confirmPassword: String): Boolean {
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

        return isValid
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}