package com.example.jikimi.presentation.fragment

import android.os.Bundle
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
import com.example.jikimi.data.model.dto.SocialItem
import com.example.jikimi.data.network.Constant
import com.example.jikimi.databinding.FragmentDetailBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.viewmodel.NaturalDisasterViewModel
import com.example.jikimi.viewmodel.SocialDisasterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailBinding? = null
    private lateinit var itemData: Item
    private val naturalDisasterViewModel: NaturalDisasterViewModel by viewModels()
    private val socialDisasterViewModel: SocialDisasterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MainActivity의 BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()

        getItemData()
        setObserveClickItem()

        getSocialItemData()
        setObserveSocialClickItem()
    }

    companion object {
        fun newInstance(item: Item): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle().apply {
                putParcelable(Constant.ITEM_DATA, item)
            }
            fragment.arguments = args
            return fragment
        }
    }


    private fun setObserveClickItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                naturalDisasterViewModel.clickItem.collect { filteredItems ->
                    updateContent(filteredItems)
                }
            }
        }
    }

    private fun setObserveSocialClickItem(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                socialDisasterViewModel.socialClickItem.collect{filteredItems ->
                    updateSocialContent(filteredItems)
                }
            }
        }
    }

    private fun getItemData() {
        arguments?.getParcelable<Item>(Constant.ITEM_DATA)?.let { item ->
            itemData = item
            updateUI(itemData)
            
            val naturalDisasterCategory = when (itemData.safetyCateNm2) {
                "태풍" -> "01001"
                "홍수" -> "01002"
                "호우" -> "01003"
                "강풍" -> "01004"
                "대설" -> "01005"
                "한파" -> "01006"
                "풍랑" -> "01007"
                "황사" -> "01008"
                "폭염" -> "01009"
                "가뭄" -> "01010"
                "지진" -> "01011"
                "지진해일" -> "01012"
                "해일" -> "01013"
                "산사태" -> "01014"
                "화산폭발" -> "01015"
                else -> return
            }
            naturalDisasterViewModel.getClickItem(naturalDisasterCategory)
        }
    }

    fun getSocialItemData(){
        val socialDisasterCategory = when (itemData.safetyCateNm2) {
            "해양오염사고" -> "02001"
            "대규모 수질오염" -> "02002"
            "식용수" -> "02003"
            "공동구 재난" -> "02004"
            "가축질병" -> "02005"
            "감염병 예방" -> "02006"
            "철도·지하철·유도선 사고" -> "02007"
            "금융전산" -> "02008"
            "원전사고" -> "02009"
            "화학물질사고" -> "02010"
            "화재" -> "02011"
            "산불" -> "02012"
            "건축물 붕괴" -> "02013"
            "댐 붕괴" -> "02014"
            "폭발" -> "02015"
            "항공기사고" -> "02016"
            "화생방사고" -> "02017"
            "정전 및 전력부족" -> "02018"
            "전기·가스사고" -> "02019"
            "유도선 사고" -> "02020"
            "수난사고" -> "02021"
            "테러" -> "02022"
            "전력수급단계별" -> "02023"
            else -> return
        }
        socialDisasterViewModel.getClickSocialItem(socialDisasterCategory)
    }

    private fun updateUI(item: Item) {
        with(binding) {
            titleTv.text = item.safetyCateNm2

            val disasterImage = when (item.safetyCateNm2) {
                "태풍" -> R.drawable.wind_img
                "홍수" -> R.drawable.flood_img
                "호우" -> R.drawable.rain_img
                "강풍" -> R.drawable.storm_img
                "대설" -> R.drawable.snow_img
                "한파" -> R.drawable.cold_img
                "풍랑" -> R.drawable.tsunami_earthquake_img
                "황사" -> R.drawable.sand_img
                "폭염" -> R.drawable.heat_img
                "가뭄" -> R.drawable.drought_img
                "지진" -> R.drawable.earthquake_img
                "지진해일" -> R.drawable.storm
                "해일" -> R.drawable.tsunami_img
                "산사태" -> R.drawable.landslide_img
                "화산폭발" -> R.drawable.volcano_img

                "해양오염사고" -> R.drawable.volcano_img
                else -> R.drawable.ic_launcher_foreground
            }

            //coil사용해서 이미지 띄우기
            detailCardViewIv.load(disasterImage){
                placeholder(R.drawable.ic_launcher_foreground)  //로딩중
                error(R.drawable.ic_launcher_foreground) // 에러발생시
            }
        }
    }

    private fun updateContent(filteredItems: List<Item>) {

        // 기본적으로 빈값을 먼저 할당
        binding.contentTitleTv2.text = ""
        binding.contentTv.text = ""
        binding.contentTitleTv3.text = ""
        binding.contentTv2.text = ""
        binding.contentTitleTv4.text = ""
        binding.contentTv3.text = ""

        // filteredItems을 순회하며, safetyCate3값을 기반으로 텍스트에 업데이트
        filteredItems.forEach { item ->
            val contentPair = when (item.safetyCate3) {
                "01001001", "01002001", "01003001", "01004001", "01005001",
                "01006001", "01007001", "01008001", "01009001", "01010001",
                "01011009", "01012005", "01013001", "01014001", "01015001", -> Pair(binding.contentTitleTv2, binding.contentTv)

                "01001002", "01002002", "01003002", "01004002", "01005002",
                "01006002", "01007002", "01008002", "01009002", "01010002",
                "01011010", "01012006", "01013002", "01014002", "01015002", -> Pair(binding.contentTitleTv3, binding.contentTv2)

                "01001003", "01002003", "01003003", "01004003", "01005003",
                "01006003", "01007003", "01008003", "01009003", "01010003",
                "01011012", "01012003", "01013003", "01014003", "01015003", -> Pair(binding.contentTitleTv4, binding.contentTv3)
                else -> null
            }

            contentPair?.let { (titleTv, contentTv) ->
                titleTv.text = item.safetyCateNm3

                // actRmks 값 필터링
                val filteredActRmks = item.actRmks?.let { actRmks ->
                    actRmks.replace("Q&amp;A", "Q&A") // Q&amp;A를 Q&A로 변경
                        .replace("&#xD;", "") // &#xD;를 제거
                } ?: "" // actRmks가 null인 경우 빈 문자열로 대체

                // 줄바꿈
                if (filteredActRmks.isNotBlank()) {
                    val currentText = contentTv.text.toString()
                    // 줄바꿈을 포함하여 텍스트를 추가
                    contentTv.text = if (currentText.isBlank()) {
                        "$filteredActRmks\n"
                    } else {
                        "$currentText\n$filteredActRmks\n"
                    }
                }
            }
        }
    }


    private fun updateSocialContent(filteredItems: List<SocialItem>) {

        // 기본적으로 빈값을 먼저 할당
        binding.contentTitleTv2.text = ""
        binding.contentTv.text = ""
        binding.contentTitleTv3.text = ""
        binding.contentTv2.text = ""
        binding.contentTitleTv4.text = ""
        binding.contentTv3.text = ""

        // filteredItems을 순회하며, safetyCate3값을 기반으로 텍스트에 업데이트
        filteredItems.forEach { item ->
            val contentPair = when (item.safetyCate3) {
                "02001001", "02002001", "02003001", "02004001", "02005001",
                "02006001", "02007001", "02008001", "02009001", "02010001",
                "02011001", "02012001", "02013001", "02014001", "02015001",
                "02016001", "02017001", "02018001", "02019001", "02020002",
                "02021002", "02022007", "02023001",  -> Pair(binding.contentTitleTv2, binding.contentTv)

                "02001002", "02002002", "02003002", "02004002", "02005002",
                "02006002", "02007002", "02008002", "02009002", "02010002",
                "02011002", "02012004", "02013003", "02014002", "02015004",
                "02016002", "02017002", "02018002", "02019002", "02020003",
                "02021003", "02022009", "02023002", -> Pair(binding.contentTitleTv3, binding.contentTv2)

                "02001003", "02002003", "02003003", "02004003", "02005004",
                "02006003", "02007003", "02008003", "02009003", "02010004",
                "02011004", "02012006", "02013005", "02014003", "02015005",
                "02016003", "02017003", "02018003", "02019004", "02020004",
                "02021009", "02022010", "02023003", -> Pair(binding.contentTitleTv4, binding.contentTv3)
                else -> null
            }

            contentPair?.let { (titleTv, contentTv) ->
                titleTv.text = item.safetyCateNm3

                // actRmks 값 필터링
                val filteredActRmks = item.actRmks?.let { actRmks ->
                    actRmks.replace("Q&amp;A", "Q&A") // Q&amp;A를 Q&A로 변경
                        .replace("&#xD;", "") // &#xD;를 제거
                } ?: "" // actRmks가 null인 경우 빈 문자열로 대체

                // 줄바꿈
                if (filteredActRmks.isNotBlank()) {
                    val currentText = contentTv.text.toString()
                    // 줄바꿈을 포함하여 텍스트를 추가
                    contentTv.text = if (currentText.isBlank()) {
                        "$filteredActRmks\n"
                    } else {
                        "$currentText\n$filteredActRmks\n"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // MainActivity의 BottomNavigationView 다시 보이게 설정
        (activity as? MainActivity)?.showBottomNavigation()
    }
}


