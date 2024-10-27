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
import com.example.jikimi.data.network.Constant
import com.example.jikimi.databinding.FragmentDetailBinding
import com.example.jikimi.viewmodel.CommonsenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDetailBinding? = null
    private lateinit var itemData: Item
    private val commonsenseViewModel: CommonsenseViewModel by viewModels()

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
        observeClickItem()
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


    private fun observeClickItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                commonsenseViewModel.clickItem.collect { filteredItems ->
                    updateContent(filteredItems)
                }
            }
        }
    }

    private fun getItemData() {
        arguments?.getParcelable<Item>(Constant.ITEM_DATA)?.let { item ->
            itemData = item
            updateUI(itemData)
            
            val categoryCode = when (itemData.safetyCateNm2) {
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
            commonsenseViewModel.getClickItem(categoryCode)
        }
    }

    private fun updateUI(item: Item) {
        with(binding) {
            titleTv.text = item.safetyCateNm2

            // 안전 카테고리와 이미지 매핑
            val imageMap = mapOf(
                "태풍" to R.drawable.wind_img,
                "호우" to R.drawable.rain_img,
                "강풍" to R.drawable.storm_img,
                "대설" to R.drawable.snow_img,
                "한파" to R.drawable.cold_img,
                "풍랑" to R.drawable.storm,
                "황사" to R.drawable.sand_img,
                "폭염" to R.drawable.heat_img,
                "가뭄" to R.drawable.drought_img,
                "산사태" to R.drawable.landslide_img,
            )

            // 해당 카테고리에 맞는 이미지 가져오기, 기본 이미지는 ic_launcher_foreground
            val image = imageMap[item.safetyCateNm2] ?: R.drawable.ic_launcher_foreground

            detailCardViewIv.load(item.contentsUrl) {
                placeholder(image)
                error(image)
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
                "01011009", "01012005", "01013001", "01014001", "01015001" -> Pair(binding.contentTitleTv2, binding.contentTv)

                "01001002", "01002002", "01003002", "01004002", "01005002",
                "01006002", "01007002", "01008002", "01009002", "01010002",
                "01011010", "01012006", "01013002", "01014002", "01015002" -> Pair(binding.contentTitleTv3, binding.contentTv2)

                "01001003", "01002003", "01003003", "01004003", "01005003",
                "01006003", "01007003", "01008003", "01009003", "01010003",
                "01011012", "01012003", "01013003", "01014003", "01015003" -> Pair(binding.contentTitleTv4, binding.contentTv3)

                else -> null
            }


            contentPair?.let { (titleView, contentView) ->
                titleView.text = item.safetyCateNm3

                // actRmks 값 필터링
                val filteredActRmks = item.actRmks?.let { actRmks ->
                    actRmks.replace("Q&amp;A", "Q&A") // Q&amp;A를 Q&A로 변경
                        .replace("&#xD;", "") // &#xD;를 제거
                } ?: "" // actRmks가 null인 경우 빈 문자열로 대체

                if (filteredActRmks.isNotBlank()) {
                    val currentText = contentView.text.toString()
                    // 줄바꿈을 포함하여 텍스트를 추가
                    contentView.text = if (currentText.isBlank()) {
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
    }
}


