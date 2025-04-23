package com.example.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.jikimi.R
import com.example.jikimi.databinding.FragmentProfileEditBinding
import com.example.jikimi.presentation.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // BottomNavigationView 다시 보이게 설정
        (activity as? MainActivity)?.showBottomNavigation()
    }
}