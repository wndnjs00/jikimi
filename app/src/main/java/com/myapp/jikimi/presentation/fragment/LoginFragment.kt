package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.myapp.jikimi.presentation.utils.showToast
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

        (activity as? MainActivity)?.hideBottomNavigation()

        setupObservers()
        setupListeners()
    }

    // viewModel에서 StateFlow로 로그인 상태를 관찰
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { resource ->
                    with(binding) {
                        when (resource) {
                            is Resource.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast("로그인 성공")

                                // 로그인 성공 시 사용자 닉네임을 SharedViewModel에 업데이트
                                resource.data?.let { user ->
                                    sharedViewModel.updateNickname(user.nickname)
                                }
                                // evacuateFragment로 이동하고 bottomNavigation 표시
                                findNavController().navigate(R.id.evacuateFragment)
                                (activity as MainActivity).showBottomNavigation()
                            }

                            is Resource.Error -> {
                                progressBar.visibility = View.GONE
                                requireContext().showToast(resource.message ?: "로그인 실패")
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
            loginBtn.setOnClickListener {
                val email = emailEt.text.toString().trim()
                val password = passwordEt.text.toString().trim()

                if (validateInputs(email, password)) {
                    viewModel.login(email, password)
                }
            }

            registerTv.setOnClickListener {
                findNavController().navigate(R.id.registerFragment)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        with(binding) {
            if (email.isEmpty()) {
                emailTil.error = "이메일을 입력하세요"
                isValid = false
            } else {
                emailTil.error = null
            }

            if (password.isEmpty()) {
                passwordTil.error = "비밀번호를 입력하세요"
                isValid = false
            } else {
                passwordTil.error = null
            }
            return isValid
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}