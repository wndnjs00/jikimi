package com.myapp.jikimi.presentation.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.databinding.FragmentCreatePostBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.presentation.adapter.ImageAdapter
import com.myapp.jikimi.presentation.utils.showToast
import com.myapp.jikimi.viewmodel.AuthViewModel
import com.myapp.jikimi.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePostFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val categories = arrayOf("긴급", "정보", "소통", "제보")
    private var selectedCategory = "소통" // Default category

    // 게시물 수정 모드 관련 변수
    private var isEditMode = false
    private var postToEdit: Post? = null
    private var postId = ""
    private var existingImageUrls = mutableListOf<String>()
    private var hideBottomNav: Boolean = false

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                if (imageAdapter.getImages().size < 5) {
                    imageAdapter.addImage(it)
                    updateImageCount()
                } else {
                    requireContext().showToast("최대 5장까지만 업로드 가능합니다")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
        hideBottomNav = arguments?.getBoolean("hideBottomNav", false) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hideBottomNav) {
            (activity as? MainActivity)?.hideBottomNavigation()
        }

        // 로그인 상태 확인
        if (!authViewModel.isLoggedIn()) {
            requireContext().showToast("로그인이 필요합니다")
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // 수정 모드인지 확인
        // 게시물 ID를 통해 구분
        arguments?.let {
            postId = it.getString("postId", "")
            isEditMode = postId.isNotEmpty()

            if (isEditMode) {
                with(binding) {
                    // 제목 변경
                    titleTv.text = "게시물 수정"
                    postBtn.text = "수정하기"
                    // 게시물 데이터 로드
                    viewModel.getPostById(postId)
                }
            }
        }
        setupObservers()
        setupListeners()
        setupRecyclerView()
        setupSpinner()
        setupBackButton()
    }

    private fun setupSpinner() {
        categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, // 기본 아이템 레이아웃
            categories
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item) // 드롭다운 아이템 레이아웃
        }

        with(binding) {
            categorySpinner.adapter = categoryAdapter
            categorySpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedCategory = categories[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // 아무것도 선택되지 않았을 때
                    }
                }
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter { position ->
            imageAdapter.removeImage(position)
            updateImageCount()
        }

        binding.imagesRv.apply {
            adapter = imageAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun updateImageCount() {
        val count = imageAdapter.getImages().size
        with(binding) {
            if (count > 0) {
                imageCountTv.visibility = View.VISIBLE
                imageCountTv.text = "$count/5"
            } else {
                imageCountTv.visibility = View.GONE
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 게시물 생성 상태 관찰
                launch {
                    viewModel.createPostStatus.collect { resource ->
                        with(binding) {
                            if (resource != null) {
                                when (resource) {
                                    is Resource.Loading -> {
                                        progressBar.visibility = View.VISIBLE
                                        postBtn.isEnabled = false
                                    }

                                    is Resource.Success -> {
                                        progressBar.visibility = View.GONE
                                        postBtn.isEnabled = true
                                        requireContext().showToast("게시물이 작성되었습니다")

                                        findNavController().navigate(R.id.communityFragment)
                                        (activity as MainActivity).showBottomNavigation()
                                        viewModel.resetCreatePostStatus()
                                    }

                                    is Resource.Error -> {
                                        progressBar.visibility = View.GONE
                                        postBtn.isEnabled = true
                                        requireContext().showToast(
                                            resource.message ?: "게시물 작성에 실패했습니다"
                                        )
                                        viewModel.resetCreatePostStatus()
                                    }
                                }
                            }
                        }
                    }
                }

                // 게시물 수정 상태 관찰
                launch {
                    viewModel.updatePostStatus.collect { resource ->
                        with(binding) {
                            if (resource != null) {
                                when (resource) {
                                    is Resource.Loading -> {
                                        progressBar.visibility = View.VISIBLE
                                        postBtn.isEnabled = false
                                    }

                                    is Resource.Success -> {
                                        progressBar.visibility = View.GONE
                                        postBtn.isEnabled = true
                                        requireContext().showToast("게시물이 수정되었습니다")

                                        findNavController().navigate(R.id.communityFragment)
                                        (activity as MainActivity).showBottomNavigation()
                                        viewModel.resetUpdatePostStatus()
                                    }

                                    is Resource.Error -> {
                                        progressBar.visibility = View.GONE
                                        postBtn.isEnabled = true
                                        requireContext().showToast(
                                            resource.message ?: "게시물 수정에 실패했습니다"
                                        )
                                        viewModel.resetUpdatePostStatus()
                                    }
                                }
                            }
                        }
                    }
                }

                // 게시물 상세 정보 관찰 (수정 모드에서 사용)
                launch {
                    viewModel.post.collect { resource ->
                        with(binding) {
                            if (resource != null) {
                                when (resource) {
                                    is Resource.Loading -> {
                                        progressBar.visibility = View.VISIBLE
                                    }

                                    is Resource.Success -> {
                                        progressBar.visibility = View.GONE
                                        postToEdit = resource.data
                                        loadPostDataForEdit()
                                    }

                                    is Resource.Error -> {
                                        progressBar.visibility = View.GONE
                                        requireContext().showToast(
                                            resource.message ?: "게시물을 불러오는데 실패했습니다"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadPostDataForEdit() {
        postToEdit?.let { post ->
            with(binding) {
                // 카테고리 설정
                val categoryIndex = categories.indexOf(post.category)
                if (categoryIndex != -1) {
                    categorySpinner.setSelection(categoryIndex)
                }
                // 내용 설정
                contentEt.setText(post.content)
                // 이미지 설정
                existingImageUrls = post.imageUrls.toMutableList()
                loadExistingImages()
            }
        }
    }

    private fun loadExistingImages() {
        // 기존 게시물의 이미지를 이미지 어댑터에 추가
        for (imageUrl in existingImageUrls) {
            // Firebase Storage URL을 Uri로 변환
            Uri.parse(imageUrl)
            imageAdapter.addFirebaseImage(imageUrl)
        }
        updateImageCount()
    }

    private fun setupListeners() {
        with(binding) {
            addImage.setOnClickListener {
                if (imageAdapter.getImages().size + imageAdapter.getFirebaseImages().size < 5) {
                    getContent.launch("image/*")
                } else {
                    requireContext().showToast("최대 5장까지만 업로드 가능합니다")
                }
            }

            postBtn.setOnClickListener {
                val content = contentEt.text.toString().trim()
                if (content.isEmpty()) {
                    requireContext().showToast("내용을 입력하세요")
                    return@setOnClickListener
                }

                if (isEditMode) {
                    // 게시물 수정 로직
                    viewModel.updatePost(
                        postId = postId,
                        content = content,
                        newImages = imageAdapter.getImages(),
                        existingImages = imageAdapter.getFirebaseImages(),
                        category = selectedCategory
                    )
                } else {
                    // 게시물 생성 로직
                    val images = imageAdapter.getImages()
                    viewModel.createPost(content, images, selectedCategory)
                }
            }
        }
    }

    private fun setupBackButton() {
        binding.backBtnIv.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (!hideBottomNav) {
            (activity as? MainActivity)?.showBottomNavigation()
        }
    }
}