package com.myapp.jikimi.presentation.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.model.entity.ShelterEntity
import com.myapp.jikimi.databinding.FragmentEvacuateBinding
import com.myapp.jikimi.presentation.adapter.ShelterSearchAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShelterSearchManager(
    private val binding: FragmentEvacuateBinding,
    private val shelterDao: ShelterDao,
    private val onShelterSelected: (ShelterEntity) -> Unit
) {
    private val searchAdapter = ShelterSearchAdapter { shelter ->
        onShelterSelected(shelter)
    }

    fun setupSearchUI() {
        // RecyclerView 설정
        binding.searchResultsRv.apply {
            layoutManager = LinearLayoutManager(binding.root.context)
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // 검색창 포커스 이벤트
        binding.searchEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchUI()
            }
        }

        // 검색창 입력 이벤트
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    searchShelters(query)
                } else {
                    showEmptyState()
                }
            }
        })

        // 검색 완료 이벤트
        binding.searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        // 오버레이 클릭하면 검색 UI 숨기기
        binding.searchOverlay.setOnClickListener {
            hideSearchUI()
        }
    }

    fun setupVoiceSearchIcon(onVoiceClick: () -> Unit) {
        binding.searchEt.setOnTouchListener { _, event ->
            val drawableEnd = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.searchEt.right - binding.searchEt.compoundDrawables[drawableEnd].bounds.width())) {
                    onVoiceClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    fun performVoiceSearch(recognizedText: String) {
        binding.searchEt.setText(recognizedText)

        if (binding.searchOverlay.visibility != View.VISIBLE) {
            showSearchUI()
        }

        if (recognizedText.isNotEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                shelterDao.searchShelters(recognizedText).collect { shelters ->
                    updateSearchResults(shelters)
                }
            }
        }
    }

    private fun showSearchUI() {
        binding.searchOverlay.visibility = View.VISIBLE
        binding.searchContainer.visibility = View.VISIBLE
        showEmptyState()
    }

    private fun showEmptyState() {
        binding.noResultsTv.text = "검색어를 입력하세요"
        binding.noResultsTv.visibility = View.VISIBLE
        binding.searchResultsRv.visibility = View.GONE
        searchAdapter.submitList(emptyList())
    }

    private fun searchShelters(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            shelterDao.searchShelters(query).collect { shelters ->
                updateSearchResults(shelters)
            }
        }
    }

    private fun updateSearchResults(shelters: List<ShelterEntity>) {
        if (shelters.isEmpty()) {
            binding.noResultsTv.text = "검색 결과가 없습니다"
            binding.noResultsTv.visibility = View.VISIBLE
            binding.searchResultsRv.visibility = View.GONE
        } else {
            binding.noResultsTv.visibility = View.GONE
            binding.searchResultsRv.visibility = View.VISIBLE
            searchAdapter.submitList(shelters)
        }
    }

    fun hideSearchUI() {
        binding.searchOverlay.visibility = View.GONE
        binding.searchContainer.visibility = View.GONE
        binding.searchEt.clearFocus()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm =
            binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEt.windowToken, 0)
    }
}
