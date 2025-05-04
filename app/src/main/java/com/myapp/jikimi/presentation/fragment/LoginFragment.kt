package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.databinding.FragmentLoginBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.viewmodel.AuthViewModel
import com.myapp.jikimi.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()

        setupObservers()
        setupListeners()
    }


    // viewModel에서 StateFlow로 로그인 상태를 관찰
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
//                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            (activity as MainActivity).showToast("로그인 성공")

                            // 로그인 성공 시 사용자 닉네임을 SharedViewModel에 업데이트
                            resource.data?.let { user ->
                                sharedViewModel.updateNickname(user.nickname)
                            }

                            // evacuateFragment로 이동하고 bottomNavigation 표시
                            findNavController().navigate(R.id.evacuateFragment)
                            (activity as MainActivity).showBottomNavigation()

                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.VISIBLE
                            (activity as MainActivity).showToast(resource.message ?: "로그인 실패")
                        }
                        else -> {} // StateFlow는 초기값이 필요하므로 null이나 기본 상태를 처리
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }


    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "이메일을 입력하세요"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "비밀번호를 입력하세요"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}