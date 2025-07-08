package com.myapp.jikimi.presentation.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.myapp.jikimi.BuildConfig
import com.myapp.jikimi.databinding.FragmentSettingBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentSettingBinding? = null

    //사용자가 설정한 모드 저장하기위해
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // SharedPreferences 초기화
        sharedPreferences =
            requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        (activity as? MainActivity)?.hideBottomNavigation()
        setupBackButton()
        setupDarkModeSwitch()
        setupAppVersion()
    }

    private fun setupBackButton() {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupDarkModeSwitch() {
        with(binding) {
            // 현재 다크모드 상태 불러오기
            val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
            switchDarkMode.isChecked = isDarkMode

            // 스위치 토글 리스너 설정
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                // SharedPreferences에 다크모드 상태 저장
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()

                // 다크모드 적용
                applyDarkMode(isChecked)
            }
        }
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            // 다크모드 적용
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            // 라이트모드 적용
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // 액티비티 재생성하여 테마 적용
        activity?.recreate()
    }

    private fun setupAppVersion() {
        // BuildConfig에서 앱 버전 정보 가져오기
        binding.appVersion.text = BuildConfig.VERSION_NAME
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity)?.showBottomNavigation()
    }
}