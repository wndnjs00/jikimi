package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.myapp.jikimi.R
import com.myapp.jikimi.databinding.FragmentDisasterDetailBinding

class DisasterDetailFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDisasterDetailBinding? = null

    private var disasterTitle: String? = null
    private var disasterCategory: String? = null
    private var disasterRiskLevel: String? = null
    private var disasterSteps: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            disasterTitle = it.getString("disaster_title")
            disasterCategory = it.getString("disaster_category")
            disasterRiskLevel = it.getString("disaster_risk_level")
            disasterSteps = it.getStringArrayList("disaster_steps")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDisasterDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackButton()
    }

    private fun setupUI() {
        // 제목 설정
        binding.disasterTitleTv.text = disasterTitle

        // 카테고리와 위험도 설정
        binding.disasterCategoryTv.text = disasterCategory
        binding.riskLevelTv.text = disasterRiskLevel

        // 위험도에 따른 색상 설정
        val riskColor = when (disasterRiskLevel) {
            "낮음" -> android.R.color.holo_green_light
            "보통" -> android.R.color.holo_orange_light
            "높음" -> android.R.color.holo_red_light
            else -> android.R.color.darker_gray
        }
        binding.riskLevelTv.setTextColor(requireContext().getColor(riskColor))

        // 상세 대처방안 설정
        setupDetailedSteps()
    }

    // 상세 대처방안 설정해야함
    private fun setupDetailedSteps() {

    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}