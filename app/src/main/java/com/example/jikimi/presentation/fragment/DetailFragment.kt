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

            val safetyCate3Map = when (titleTv.text) {
                "태풍" -> arrayOf("01001001", "01001002", "01001003")
                "홍수" -> arrayOf("01002001", "01002002", "01002003")
                "호우" -> arrayOf("01003001", "01003002", "01003003")
                "강풍" -> arrayOf("01004001", "01004002", "01004003")
                "대설" -> arrayOf("01005001", "01005002", "01005003")
                "한파" -> arrayOf("01006001", "01006002", "01006003")
                "풍랑" -> arrayOf("01007001", "01007002", "01007003")
                "황사" -> arrayOf("01008001", "01008002", "01008003")
                "폭염" -> arrayOf("01009001", "01009002", "01009003")
                "가뭄" -> arrayOf("01010001", "01010002", "01010003")
                "지진" -> arrayOf("01011001", "01011002", "01011003")
                "지진해일" -> arrayOf("01012001", "01012002", "01012003")
                "해일" -> arrayOf("01013001", "01013002", "01013003")
                "산사태" -> arrayOf("01014001", "01014002", "01014003")
                "화산폭발" -> arrayOf("01015001", "01015002", "01015003")
                else -> emptyArray()
            }

            safetyCate3Map.forEachIndexed { index, safetyCate3 ->
                allItems.find { it.safetyCate3 == safetyCate3 }?.let { item ->
                    when (index) {
                        0 -> {
                            contentTitleTv2.text = item.safetyCateNm3
                            contentTv.text = item.actRmks
                        }
                        1 -> {
                            contentTitleTv3.text = item.safetyCateNm3
                            contentTv2.text = item.actRmks
                        }
                        2 -> {
                            contentTitleTv4.text = item.safetyCateNm3
                            contentTv3.text = item.actRmks
                        }
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
