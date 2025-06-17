package com.myapp.jikimi.presentation.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.myapp.jikimi.R
import com.myapp.jikimi.data.network.dpToPx
import com.myapp.jikimi.databinding.FragmentDisasterDetailBinding
import com.myapp.jikimi.presentation.activity.MainActivity

class DisasterDetailFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDisasterDetailBinding? = null

    private var disasterTitle: String? = null
    private var disasterSubTitle: String? = null
    private var disasterCategory: String? = null
    private var disasterRiskLevel: String? = null
    private var disasterSteps: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            disasterTitle = it.getString("disaster_title")
            disasterSubTitle = it.getString("disaster_subtitle")
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

        // BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()
        setupUI()
        setupBackButton()
    }

    private fun setupUI() {
        // 제목 설정
        binding.disasterTitleTv.text = disasterTitle
        binding.disasterSubtitleTv.text = disasterSubTitle

        // 카테고리와 위험도 설정
        binding.disasterCategoryTv.text = disasterCategory
        binding.riskLevelTv.text = disasterRiskLevel

        // 상세 대처방안 설정
        setupDetailedSteps()
    }

    private fun setupDetailedSteps() {
        val container = binding.linearLayoutSteps
        container.removeAllViews() // 기존 뷰 제거

        disasterSteps?.forEachIndexed { index, stepText ->
            val context = requireContext()

            // 외부 LinearLayout 생성
            val stepLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                orientation = LinearLayout.HORIZONTAL
                setPadding(12.dpToPx(context), 12.dpToPx(context), 12.dpToPx(context), 12.dpToPx(context))
                gravity = Gravity.CENTER_VERTICAL
            }

            // 숫자 동그라미 TextView 생성
            val numberTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(24.dpToPx(context), 24.dpToPx(context))
                text = (index + 1).toString()
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.circle_bg_red)
                setTypeface(null, Typeface.BOLD)
                textSize = 12f
            }

            // 설명 텍스트 TextView 생성
            val stepTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(16.dpToPx(context), 0, 0, 0)
                }
                text = stepText
                setTextColor(Color.parseColor("#333333"))
                textSize = 16f
            }

            stepLayout.addView(numberTextView)
            stepLayout.addView(stepTextView)

            container.addView(stepLayout)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        (activity as MainActivity)?.showBottomNavigation()
    }
}