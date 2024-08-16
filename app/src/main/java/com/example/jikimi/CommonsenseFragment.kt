package com.example.jikimi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jikimi.databinding.FragmentCommonsenseBinding
import com.example.jikimi.databinding.FragmentEvacuateBinding


class CommonsenseFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentCommonsenseBinding? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommonsenseBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}