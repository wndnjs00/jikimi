package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.CommonsenseItem
import com.myapp.jikimi.databinding.FragmentCommonsenseBinding
import com.myapp.jikimi.databinding.FragmentCommunityBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.presentation.adapter.CommonsenseAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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


        // 임의의 데이터 생성 (삭제할것)
        val sampleData = listOf(
            CommonsenseItem("지진 발생 시 대처방법", "갑작스런 지진 상황에서의 생존 가이드", "긴급재해", "위험도: 높음"),
            CommonsenseItem("화재 발생 시 대처방법", "아파트 화재 대처법과 안전한 대피 방법", "긴급재해", "위험도: 중간"),
            CommonsenseItem("홍수 시 대처방법", "홍수 발생 시 안전하게 피난하는 방법", "자연재해", "위험도: 낮음")
        )

        // 어댑터와 레이아웃 매니저 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = CommonsenseAdapter(sampleData)

        // 임시로 글씨클릭했을때 detail로 이동하도록
        binding.titleTv.setOnClickListener {
            findNavController().navigate(R.id.commonsenseDetailFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
