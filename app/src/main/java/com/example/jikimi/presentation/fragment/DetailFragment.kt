package com.example.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.network.Constant
import com.example.jikimi.databinding.FragmentDetailBinding
import com.example.jikimi.databinding.FragmentEvacuateBinding
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailBinding? = null
    private lateinit var itemData : Item


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getItemData()
    }


    companion object {
        // DetailFragment로 데이터 전달
        fun newInstance(item: Item): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle().apply {
                putParcelable(Constant.ITEM_DATA, item)
            }
            fragment.arguments = args
            return fragment
        }
    }


    // 전달된 데이터 받기
    private fun getItemData(){
        arguments?.let { item ->
            itemData = item.getParcelable(Constant.ITEM_DATA) ?: return@let
            updateUI(itemData)
        }
    }


    private fun updateUI(item: Item) {
        with(binding) {
            detailCardViewIv.load(item.contentsUrl) {
                placeholder(R.drawable.ic_launcher_foreground)  // 로딩중
                error(R.drawable.ic_launcher_foreground)        // 에러발생시
            }
            titleTv.text = item.safetyCateNm2
            contentTitleTv2.text = item.safetyCateNm3
            contentTv.text = item.actRmks
            contentTitleTv3.text = item.safetyCateNm3
            contentTv2.text = item.actRmks
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}