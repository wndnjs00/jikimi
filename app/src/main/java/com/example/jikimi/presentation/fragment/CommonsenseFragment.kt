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
import com.example.jikimi.databinding.FragmentCommonsenseBinding
import com.example.jikimi.presentation.ChipType
import com.example.jikimi.presentation.adapter.CommonsenseAdapter
import com.example.jikimi.viewmodel.CommonsenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommonsenseFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentCommonsenseBinding? = null

    private val commonsenseViewModel : CommonsenseViewModel by viewModels()

    private val commonsenseAdapter : CommonsenseAdapter by lazy{
        CommonsenseAdapter(

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
            ChipType.SECOND -> commonsenseViewModel.getNaturalDisaster(safetyCate = "01001")
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

                commonsenseAdapter.submitList(disasterItems)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}