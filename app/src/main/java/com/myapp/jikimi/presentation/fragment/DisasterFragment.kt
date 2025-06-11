package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.databinding.FragmentDisasterBinding
import com.myapp.jikimi.presentation.adapter.DisasterAdapter
import com.myapp.jikimi.viewmodel.DisasterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisasterFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentDisasterBinding? = null
    private val viewModel: DisasterViewModel by viewModels()
    private lateinit var disasterAdapter: DisasterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDisasterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

    }


    private fun setupRecyclerView() {
        disasterAdapter = DisasterAdapter { disaster, position ->
            // 리사이클러뷰 아이템 클릭 시 상세 화면으로 이동
            val bundle = Bundle().apply {
                putString("disaster_title", disaster.title)
                putString("disaster_category", disaster.category)
                putString("disaster_risk_level", disaster.riskLevel)
                putStringArrayList("disaster_steps", ArrayList(disaster.detailedSteps))
            }
            findNavController().navigate(R.id.disasterDetailFragment, bundle)
        }

        binding.recyclerView.apply {
            adapter = disasterAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

//    private fun setupSearchView() {
//        binding.searchEt.setOnFocusChangeListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let {
//                    if (it.isNotBlank()) {
//                        viewModel.searchDisaster(it)
//                    }
//                }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (newText.isNullOrBlank()) {
//                    viewModel.clearSearchResult()
//                }
//                return true
//            }
//        })
//    }

    private fun observeViewModel() {
        // 오늘의 재난 정보 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayDisasters.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
//                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }

                    is Resource.Success -> {
//                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        disasterAdapter.submitList(resource.data)
                    }

                    is Resource.Error -> {
//                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        // 검색 결과 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResult.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
//                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    is Resource.Success -> {
//                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        disasterAdapter.submitList(listOf(resource.data))
                    }
                    is Resource.Error -> {
//                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        // 검색 결과가 클리어된 경우 원래 목록으로 복원
                        // 이미 todayDisasters가 관찰되고 있으므로 별도 처리 불필요
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
