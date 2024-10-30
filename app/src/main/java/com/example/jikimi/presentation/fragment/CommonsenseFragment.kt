package com.example.jikimi.presentation.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.model.dto.Items
import com.example.jikimi.data.model.dto.SocialItem
import com.example.jikimi.databinding.FragmentCommonsenseBinding
import com.example.jikimi.presentation.ChipType
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.presentation.adapter.CommonsenseAdapter
import com.example.jikimi.viewmodel.NaturalDisasterViewModel
import com.example.jikimi.viewmodel.SocialDisasterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommonsenseFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentCommonsenseBinding? = null

    private val naturalDisasterViewModel : NaturalDisasterViewModel by viewModels()
    private val socialDisasterViewModel : SocialDisasterViewModel by viewModels()

    private lateinit var itemData : Item

    private var disasterItems: MutableList<Item> = mutableListOf()
    private var socialDisasterItems : MutableList<SocialItem> = mutableListOf()

    private val commonsenseAdapter : CommonsenseAdapter by lazy{
        CommonsenseAdapter(
            onClick = { item, position ->
                itemData = item

                // DetailFragment로 데이터를 전달하면서 이동
                val detailFragment = DetailFragment.newInstance(itemData)
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_main, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommonsenseBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipGroup()
        setRecyclerView()
        naturalDisasterSetObserve()
        socialDisasterSetObserve()
    }


    private fun chipGroup(){
        binding.chipGroup.setOnCheckedStateChangeListener{ chipGroup, checkedId ->
            val selectChip = chipGroup.checkedChipId

            when(selectChip){
                R.id.fist_type -> chipType(ChipType.FIRST)
                R.id.second_type -> chipType(ChipType.SECOND)
                R.id.three_type -> chipType(ChipType.THIRD)
                R.id.four_type -> chipType(ChipType.FOURTH)
                else -> Log.d("chipGroup", "존재하지 않음")
            }
        }
    }

    private fun chipType(type: ChipType){
        when(type){
            ChipType.FIRST -> Toast.makeText(requireContext(), "ViewModel로 전체데이터 가져오기", Toast.LENGTH_SHORT).show()
            ChipType.SECOND -> {
                if(disasterItems.isEmpty()){
                    val safetyCates = listOf("01001","01002","01003","01004","01005","01006","01007","01008","01009","01010","01011","01012","01013","01014","01015")
                    naturalDisasterViewModel.getNaturalDisaster(safetyCates)
                }
            }
            ChipType.THIRD -> {
                if(socialDisasterItems.isEmpty()){
                    val socialSafetyCates = listOf("02001","02002","02003","02004","02005","02006","02007","02008","02009","02010","02011","02012","02013","02014","02015","02016","02017","02018","02019","02020","02021","02022","02023")
                    socialDisasterViewModel.getSocialDisaster(socialSafetyCates)
                }
            }
            ChipType.FOURTH -> Toast.makeText(requireContext(), "ViewModel로 생활재난 데이터 가져오기", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setRecyclerView(){
        with(binding.commonsenseRecyclerview){
            adapter = commonsenseAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun naturalDisasterSetObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                naturalDisasterViewModel.naturalDisaster.collect { item ->
                    item?.let{
                        if (!disasterItems.contains(it)) { // 중복 체크
                            disasterItems.add(it)
                            commonsenseAdapter.submitList(disasterItems.toList())
                        }
                    }
                }
            }
        }
    }

    private fun socialDisasterSetObserve(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                socialDisasterViewModel.socialDisaster.collect{ item ->
                    item?.let{
                        if(!socialDisasterItems.contains(it)){
                            socialDisasterItems.add(it)

                            val itemList = socialDisasterItems.map{ socialItem ->
                                Item(
                                    actRmks = socialItem.actRmks,
                                    contentsType = socialItem.contentsType,
                                    mainOrd = socialItem.mainOrd,
                                    safetyCate1 = socialItem.safetyCate1,
                                    safetyCate2 = socialItem.safetyCate2,
                                    safetyCate3 = socialItem.safetyCate3,
                                    safetyCateNm1 = socialItem.safetyCateNm1,
                                    safetyCateNm2 = socialItem.safetyCateNm2,
                                    safetyCateNm3 = socialItem.safetyCateNm3,
                                    subOrd = socialItem.subOrd,
                                    contentsUrl = socialItem.contentsUrl,
                                )
                            }
                            commonsenseAdapter.submitList(itemList)
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