package com.example.jikimi.presentation.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.model.dto.Items
import com.example.jikimi.data.network.Constant
import com.example.jikimi.databinding.FragmentDetailBinding
import com.example.jikimi.databinding.FragmentEvacuateBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.viewmodel.CommonsenseViewModel
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailBinding? = null
    private lateinit var itemData : Item
    private val commonsenseViewModel : CommonsenseViewModel by viewModels()


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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                // Observe clickItem in ViewModel
                commonsenseViewModel.clickItem.collect { filteredItems ->
                    updateContent(filteredItems)
                }
            }
        }
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

            Log.d("itemData", "itemData: $itemData")
            updateUI(itemData)

            when(itemData.safetyCateNm2){
                "태풍" -> commonsenseViewModel.getClickItem("01001")
                "홍수" -> commonsenseViewModel.getClickItem("01002")
                "호우" -> commonsenseViewModel.getClickItem("01003")
                "강풍" -> commonsenseViewModel.getClickItem("01004")
                "대설" -> commonsenseViewModel.getClickItem("01005")
                "한파" -> commonsenseViewModel.getClickItem("01006")
                "풍랑" -> commonsenseViewModel.getClickItem("01007")
                "황사" -> commonsenseViewModel.getClickItem("01008")
                "폭염" -> commonsenseViewModel.getClickItem("01009")
                "가뭄" -> commonsenseViewModel.getClickItem("01010")
                "지진" -> commonsenseViewModel.getClickItem("01011")
                "지진해일" -> commonsenseViewModel.getClickItem("01012")
                "해일" -> commonsenseViewModel.getClickItem("01013")
                "산사태" -> commonsenseViewModel.getClickItem("01014")
                "화산폭발" -> commonsenseViewModel.getClickItem("01015")
            }
        }
    }


    private fun updateUI(item: Item) {
        with(binding) {
            detailCardViewIv.load(item.contentsUrl) {
                placeholder(R.drawable.ic_launcher_foreground)  // 로딩중
                error(R.drawable.ic_launcher_foreground)        // 에러발생시
            }
            titleTv.text = item.safetyCateNm2

        }
    }


//    private fun updateContent(filteredItems: List<Item>) {
//        // Filter and update content based on safetyCate3 values
//        filteredItems.forEach { item ->
//            when (item.safetyCate3) {
//                "01001001" -> {
//                    binding.contentTitleTv2.text = item.safetyCateNm3
//                    binding.contentTv.text = item.actRmks
//                }
//                "01001002" -> {
//                    binding.contentTitleTv3.text = item.safetyCateNm3
//                    binding.contentTv2.text = item.actRmks
//                }
//                "01001003" -> {
//                    binding.contentTitleTv4.text = item.safetyCateNm3
//                    binding.contentTv3.text = item.actRmks
//                }
//                "01002001" -> {
//                    binding.contentTitleTv2.text = item.safetyCateNm3
//                    binding.contentTv.text = item.actRmks
//                }
//                "01002002" -> {
//                    binding.contentTitleTv3.text = item.safetyCateNm3
//                    binding.contentTv2.text = item.actRmks
//                }
//                "01002003" -> {
//                    binding.contentTitleTv4.text = item.safetyCateNm3
//                    binding.contentTv3.text = item.actRmks
//                }
//                "01003001" -> {
//                    binding.contentTitleTv2.text = item.safetyCateNm3
//                    binding.contentTv.text = item.actRmks
//                }
//                "01003002" -> {
//                    binding.contentTitleTv3.text = item.safetyCateNm3
//                    binding.contentTv2.text = item.actRmks
//                }
//                "01003003" -> {
//                    binding.contentTitleTv4.text = item.safetyCateNm3
//                    binding.contentTv3.text = item.actRmks
//                }
//                "01004001" -> {
//                    binding.contentTitleTv2.text = item.safetyCateNm3
//                    binding.contentTv.text = item.actRmks
//                }
//                "01004002" -> {
//                    binding.contentTitleTv3.text = item.safetyCateNm3
//                    binding.contentTv2.text = item.actRmks
//                }
//                "01004003" -> {
//                    binding.contentTitleTv4.text = item.safetyCateNm3
//                    binding.contentTv3.text = item.actRmks
//                }
//            }
//        }
//    }


    private fun updateContent(filteredItems: List<Item>) {
        filteredItems.forEach { item ->
            // safetyCate3에 따라 제목과 내용을 설정
            val (titleTextView, contentTextView) = when (item.safetyCate3) {
                "01001001", "01002001", "01003001", "01004001", "01005001", "01006001", "01007001" , "01008001", "01009001", "01010001", "01011009", "01012005", "01013001", "01014001", "01015001"-> Pair(binding.contentTitleTv2, binding.contentTv)
                "01001002", "01002002", "01003002", "01004002", "01005002", "01006002", "01007002" , "01008002", "01009002", "01010002", "01011010", "01012006", "01013002", "01014002", "01015002" -> Pair(binding.contentTitleTv3, binding.contentTv2)
                "01001003", "01002003", "01003003", "01004003", "01005003", "01006003", "01007003" , "01008003", "01009003", "01010003", "01011012", "01012003", "01013003", "01014003", "01015003" -> Pair(binding.contentTitleTv4, binding.contentTv3)
                else -> return@forEach // 조건에 맞지 않으면 다음으로 넘어감
            }
            // 공통적으로 적용되는 설정
            titleTextView.text = item.safetyCateNm3
            contentTextView.text = item.actRmks
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
