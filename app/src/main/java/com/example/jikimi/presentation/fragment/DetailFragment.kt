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
    private lateinit var allItems: List<Item>


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
        fun newInstance(item: Item, allItems: List<Item>): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle().apply {
                putParcelable(Constant.ITEM_DATA, item)
                putParcelableArrayList(Constant.ALL_ITEMS, ArrayList(allItems))
            }
            fragment.arguments = args
            return fragment
        }
    }


    // 전달된 데이터 받기
    private fun getItemData(){
        arguments?.let { item ->
            itemData = item.getParcelable(Constant.ITEM_DATA) ?: return@let
            allItems = item.getParcelableArrayList(Constant.ALL_ITEMS) ?: emptyList()
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


            when (titleTv.text) {
                "태풍" -> {
                    allItems.find { it.safetyCate3 == "01001001" }?.let { item ->
                        contentTitleTv2.text = item.safetyCateNm3
                        contentTv.text = item.actRmks
                    }

                    allItems.find { it.safetyCate3 == "01001002" }?.let { item ->
                        contentTitleTv3.text = item.safetyCateNm3
                        contentTv2.text = item.actRmks
                    }

                    allItems.find { it.safetyCate3 == "01001003" }?.let { item ->
                        contentTitleTv4.text = item.safetyCateNm3
                        contentTv3.text = item.actRmks
                    }
                }
                "홍수" -> {
                    allItems.find { it.safetyCate3 == "01002001" }?.let { item ->
                        contentTitleTv2.text = item.safetyCateNm3
                        contentTv.text = item.actRmks
                    }
                    allItems.find { it.safetyCate3 == "01002002" }?.let { item ->
                        contentTitleTv3.text = item.safetyCateNm3
                        contentTv2.text = item.actRmks
                    }
                    allItems.find { it.safetyCate3 == "01002003" }?.let { item ->
                        contentTitleTv4.text = item.safetyCateNm3
                        contentTv3.text = item.actRmks
                    }
                }
                "호우" -> {
                    allItems.find { it.safetyCate3 == "01003001" }?.let { item ->
                        contentTitleTv2.text = item.safetyCateNm3
                        contentTv.text = item.actRmks
                    }
                    allItems.find { it.safetyCate3 == "01003002" }?.let { item ->
                        contentTitleTv3.text = item.safetyCateNm3
                        contentTv2.text = item.actRmks
                    }
                    allItems.find { it.safetyCate3 == "01003003" }?.let { item ->
                        contentTitleTv4.text = item.safetyCateNm3
                        contentTv3.text = item.actRmks
                    }
                }
                "강풍" -> {
                    allItems.find { it.safetyCate3 == "01004001" }?.let { item ->
                        contentTitleTv2.text = item.safetyCateNm3
                        contentTv.text = item.actRmks
                    }
                    allItems.find { it.safetyCate3 == "01004002" }?.let { item ->
                        contentTitleTv3.text = item.safetyCateNm3
                        contentTv2.text = item.actRmks
                    }
                    allItems.find { it.safetyCate3 == "01004003" }?.let { item ->
                        contentTitleTv4.text = item.safetyCateNm3
                        contentTv3.text = item.actRmks
                    }
                }
                else -> {""}
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
