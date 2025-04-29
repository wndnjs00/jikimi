package com.example.jikimi.presentation.fragment

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jikimi.R
import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.Post
import com.example.jikimi.databinding.FragmentCreatePostBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.presentation.adapter.ImageAdapter
import com.example.jikimi.viewmodel.AuthViewModel
import com.example.jikimi.viewmodel.PostViewModel
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

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (imageAdapter.getImages().size < 5) {
                imageAdapter.addImage(it)
                updateImageCount()
            } else {
                (activity as MainActivity).showToast("최대 5장까지만 업로드 가능합니다")
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

        // BottomNavigationView 숨기기
        if (hideBottomNav) {
            (activity as? MainActivity)?.hideBottomNavigation()
        }

        // 로그인 상태 확인
        if (!authViewModel.isLoggedIn()) {
            (activity as MainActivity).showToast("로그인이 필요합니다")
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // 수정 모드인지 확인
        // 게시물 ID를 통해 구분
        arguments?.let {
            postId = it.getString("postId", "")
            isEditMode = postId.isNotEmpty()

            if (isEditMode) {
                // 제목 변경
                binding.tvTitle.text = "게시물 수정"
                binding.btnPost.text = "수정하기"

                // 게시물 데이터 로드
                viewModel.getPostById(postId)
            }
        }

        setupObservers()
        setupListeners()
        setupRecyclerView()
        setupSpinner()
    }

    private fun setupSpinner() {
        categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, // 기본 아이템 레이아웃
            categories
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item) // 드롭다운 아이템 레이아웃
        }

        binding.spinnerCategory.adapter = categoryAdapter
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무것도 선택되지 않았을 때
            }
        }
    }


    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter { position ->
            imageAdapter.removeImage(position)
            updateImageCount()
        }

        binding.rvImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // 아이템 간 간격 추가 (선택사항)
//            addItemDecoration(object : RecyclerView.ItemDecoration() {
//                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
//                    outRect.right = resources.getDimensionPixelSize(R.dimen.item_margin) // 적절한 마진값 리소스 필요
//                }
//            })
        }
    }

    private fun updateImageCount() {
        val count = imageAdapter.getImages().size
        if (count > 0) {
            binding.tvImageCount.visibility = View.VISIBLE
            binding.tvImageCount.text = "$count/5"
        } else {
            binding.tvImageCount.visibility = View.GONE
        }
    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 게시물 생성 상태 관찰
                launch {
                    viewModel.createPostStatus.collect { resource ->
                        if (resource != null) {
                            when (resource) {
                                is Resource.Loading -> {
                                    binding.progressBar.visibility = View.VISIBLE
                                    binding.btnPost.isEnabled = false
                                }
                                is Resource.Success -> {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnPost.isEnabled = true
                                    (activity as MainActivity).showToast("게시물이 작성되었습니다")

                                    findNavController().navigate(R.id.commonsenseFragment)
                                    (activity as MainActivity).showBottomNavigation()

                                    viewModel.resetCreatePostStatus()
                                }
                                is Resource.Error -> {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnPost.isEnabled = true
                                    (activity as MainActivity).showToast(resource.message ?: "게시물 작성에 실패했습니다")
                                    viewModel.resetCreatePostStatus()
                                }
                            }
                        }
                    }
                }

                // 게시물 수정 상태 관찰
                launch {
                    viewModel.updatePostStatus.collect { resource ->
                        if (resource != null) {
                            when (resource) {
                                is Resource.Loading -> {
                                    binding.progressBar.visibility = View.VISIBLE
                                    binding.btnPost.isEnabled = false
                                }
                                is Resource.Success -> {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnPost.isEnabled = true
                                    (activity as MainActivity).showToast("게시물이 수정되었습니다")

                                    findNavController().navigate(R.id.commonsenseFragment)
                                    (activity as MainActivity).showBottomNavigation()

                                    viewModel.resetUpdatePostStatus()
                                }
                                is Resource.Error -> {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnPost.isEnabled = true
                                    (activity as MainActivity).showToast(resource.message ?: "게시물 수정에 실패했습니다")
                                    viewModel.resetUpdatePostStatus()
                                }
                            }
                        }
                    }
                }

                // 게시물 상세 정보 관찰 (수정 모드에서 사용)
                launch {
                    viewModel.post.collect { resource ->
                        if (resource != null) {
                            when (resource) {
                                is Resource.Loading -> {
                                    binding.progressBar.visibility = View.VISIBLE
                                }
                                is Resource.Success -> {
                                    binding.progressBar.visibility = View.GONE
                                    postToEdit = resource.data
                                    loadPostDataForEdit()
                                }
                                is Resource.Error -> {
                                    binding.progressBar.visibility = View.GONE
                                    (activity as MainActivity).showToast(resource.message ?: "게시물을 불러오는데 실패했습니다")
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
            // 카테고리 설정
            val categoryIndex = categories.indexOf(post.category)
            if (categoryIndex != -1) {
                binding.spinnerCategory.setSelection(categoryIndex)
            }

            // 내용 설정
            binding.etContent.setText(post.content)

            // 이미지 설정
            existingImageUrls = post.imageUrls.toMutableList()
            loadExistingImages()
        }
    }

    private fun loadExistingImages() {
        // 기존 게시물의 이미지를 이미지 어댑터에 추가
        for (imageUrl in existingImageUrls) {
            // Firebase Storage URL을 Uri로 변환
            val imageUri = Uri.parse(imageUrl)
            imageAdapter.addFirebaseImage(imageUrl)
        }
        updateImageCount()
    }


    private fun setupListeners() {
        binding.btnAddImage.setOnClickListener {
            if (imageAdapter.getImages().size + imageAdapter.getFirebaseImages().size < 5) {
                getContent.launch("image/*")
            } else {
                (activity as MainActivity).showToast("최대 5장까지만 업로드 가능합니다")
            }
        }

        binding.btnPost.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            if (content.isEmpty()) {
                (activity as MainActivity).showToast("내용을 입력하세요")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (!hideBottomNav) {
            (activity as? MainActivity)?.showBottomNavigation()
        }
    }
}