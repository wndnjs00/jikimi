package com.myapp.jikimi.presentation.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.ViewUtils.hideKeyboard
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
        setupSearchView()
    }


    private fun setupRecyclerView() {
        disasterAdapter = DisasterAdapter { disaster, position ->
            // 리사이클러뷰 아이템 클릭 시 상세 화면으로 이동
            val bundle = Bundle().apply {
                putString("disaster_title", disaster.title)
                putString("disaster_subtitle", disaster.subtitle)
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

        private fun observeViewModel() {
        // 오늘의 재난 정보 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayDisasters.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading(true)
                        showEmptyResult(false)
                    }

                    is Resource.Success -> {
                        showLoading(false)
                        showEmptyResult(false)
                        disasterAdapter.submitList(resource.data)
                    }

                    is Resource.Error -> {
                        showLoading(false)
                        showEmptyResult(false)
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
                        showLoading(true)
                        showEmptyResult(false)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        showEmptyResult(false)
                        disasterAdapter.submitList(listOf(resource.data))
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        showEmptyResult(false)
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        // 검색 결과가 클리어된 경우 - 오늘의 재난 목록 다시 로드
                        viewModel.todayDisasters.value.let { todayResource ->
                            if (todayResource is Resource.Success) {
                                showLoading(false)
                                disasterAdapter.submitList(todayResource.data)
                            }
                        }
                    }
                }
            }
        }
            // 검색 결과 없음 상태 관찰
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isSearchEmpty.collect { isEmpty ->
                    if (isEmpty) {
                        showLoading(false)
                        showEmptyResult(true)
                    }else{
                        showEmptyResult(false)
                    }
                }
            }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchView() {
        // 검색 입력 처리
        binding.searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEt.text.toString().trim()
                performSearch(query)
                // 키보드 숨기기
                hideKeyboard()
                true
            } else {
                false
            }
        }

        // X 버튼 클릭 처리 (drawableEnd)
        binding.searchEt.setOnTouchListener { _, event ->
            val drawableEnd = 2 // RIGHT drawable index
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = binding.searchEt.compoundDrawables[drawableEnd]
                if (drawable != null) {
                    val drawableWidth = drawable.intrinsicWidth
                    val clickAreaStart = binding.searchEt.right - binding.searchEt.paddingRight - drawableWidth

                    if (event.rawX >= clickAreaStart) {
                        // X 버튼 클릭됨 - 검색 초기화
                        clearSearch()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        // 텍스트 변경 감지
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    // 검색어가 비워지면 원래 목록으로 복원
                    viewModel.clearSearchResult()
                    updateTitle("오늘의 재난 대처법")
                }
            }
        })
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            viewModel.searchDisaster(query)
            updateTitle("'$query' 검색결과")
        }
    }

    private fun clearSearch() {
        binding.searchEt.text.clear()
        viewModel.clearSearchResult()
        updateTitle("오늘의 재난 대처법")
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEt.windowToken, 0)
    }

    private fun updateTitle(title: String) {
        binding.titleTv.text = title
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.loadingLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showEmptyResult(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyResultLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.loadingLayout.visibility = View.GONE
        } else {
            binding.emptyResultLayout.visibility = View.GONE
        }
    }

        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
