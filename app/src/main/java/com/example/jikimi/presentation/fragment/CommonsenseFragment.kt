package com.example.jikimi.presentation.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.databinding.FragmentCommonsenseBinding
import com.example.jikimi.presentation.ChipType
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.presentation.adapter.CommonsenseAdapter
import com.example.jikimi.viewmodel.CommonsenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommonsenseFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentCommonsenseBinding? = null

    private val commonsenseViewModel : CommonsenseViewModel by viewModels()
    private lateinit var itemData : Item
    private lateinit var disasterItems: List<Item>

    private val commonsenseAdapter : CommonsenseAdapter by lazy{
        CommonsenseAdapter(
            onClick = { item, position ->
                itemData = item
                // DetailFragment로 item을 전달하면서 이동
                val detailFragment = DetailFragment.newInstance(itemData, disasterItems)
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
        setObserve()
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
                val safetyCates = listOf("01001","01002","01003","01004","01005","01006","01007","01008","01009","01010","01011","01012","01013","01014","01015")
                commonsenseViewModel.getNaturalDisaster(safetyCates)
            }
            ChipType.THIRD -> Toast.makeText(requireContext(), "ViewModel로 사회재난 데이터 가져오기", Toast.LENGTH_SHORT).show()
            ChipType.FOURTH -> Toast.makeText(requireContext(), "ViewModel로 생활재난 데이터 가져오기", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setRecyclerView(){
        with(binding.commonsenseRecyclerview){
            adapter = commonsenseAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setObserve(){
        viewLifecycleOwner.lifecycleScope.launch {
            commonsenseViewModel.naturalDisaster.collect{ disasterItems ->
                this@CommonsenseFragment.disasterItems = disasterItems
                commonsenseAdapter.submitList(disasterItems)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}