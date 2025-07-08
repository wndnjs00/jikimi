package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.data.model.dto.User
import com.myapp.jikimi.databinding.FragmentCommunityBinding
import com.myapp.jikimi.presentation.adapter.PostAdapter
import com.myapp.jikimi.presentation.utils.showToast
import com.myapp.jikimi.viewmodel.AuthViewModel
import com.myapp.jikimi.viewmodel.EvacuationMessageViewModel
import com.myapp.jikimi.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommunityFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentCommunityBinding? = null
    private val postViewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val evacuationMessageViewModel: EvacuationMessageViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    // 카테고리 관련 변수
    private var allPosts = listOf<Post>()
    private var currentFilter = "전체"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupChipGroup()
        // 게시물 목록 불러오기
        postViewModel.getPosts()
        loadUserProfile()
        // 재난 안내문자 데이터 가져오기
        evacuationMessageViewModel.getLatestEvacuationMessage()
    }

    private fun setupRecyclerView() {
        // 로그인한 user의 uid를 가져옴
        val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""

        // 게시물클릭시, postId전달하면서 detailFragment로 이동
        postAdapter = PostAdapter(
            onPostClick = { post ->
                val bundle = Bundle().apply {
                    putString("postId", post.id)
                }
                findNavController().navigate(R.id.detailFragment, bundle)
            },
            currentUserId = currentUserId
        )
        binding.postRv.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 게시물 목록 상태 관찰
                launch {
                    postViewModel.posts.collect { resource ->
                        with(binding) {
                            when (resource) {
                                is Resource.Loading -> {
                                    progressBar.visibility = View.VISIBLE
                                }

                                is Resource.Success -> {
                                    progressBar.visibility = View.GONE
                                    allPosts = resource.data ?: emptyList()
                                    filterPosts() // 필터링 적용
                                }

                                is Resource.Error -> {
                                    progressBar.visibility = View.GONE
                                    requireContext().showToast(
                                        resource.message ?: "게시물을 불러오는데 실패했습니다"
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
                // 게시물 삭제 상태 관찰
                launch {
                    postViewModel.deletePostStatus.collect { resource ->
                        with(binding) {
                            if (resource != null) {
                                when (resource) {
                                    is Resource.Loading -> {
                                        progressBar.visibility = View.VISIBLE
                                    }

                                    is Resource.Success -> {
                                        progressBar.visibility = View.GONE
                                        requireContext().showToast("게시물이 삭제되었습니다")
                                        postViewModel.getPosts() // 목록 새로고침
                                        postViewModel.resetDeletePostStatus() // 상태 리셋
                                    }

                                    is Resource.Error -> {
                                        progressBar.visibility = View.GONE
                                        requireContext().showToast(
                                            resource.message ?: "게시물 삭제에 실패했습니다"
                                        )
                                        postViewModel.resetDeletePostStatus() // 상태 리셋
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
                // 재난 안내문자 상태 관찰
                launch {
                    evacuationMessageViewModel.evacuationMessage.collect { resource ->
                        with(binding) {
                            when (resource) {
                                is Resource.Loading -> {
                                    dateContentTv.visibility = View.GONE
                                    messageContentTv.text = "로딩 중..."
                                }

                                is Resource.Success -> {
                                    val message = resource.data
                                    if (message != null) {
                                        dateContentTv.visibility = View.VISIBLE
                                        dateContentTv.text = message.createdDateTime
                                        messageContentTv.text = message.messageContent
                                    } else {
                                        dateContentTv.visibility = View.GONE
                                        messageContentTv.text = "최근 발령된 재난문자가 없습니다"
                                    }
                                }

                                is Resource.Error -> {
                                    dateContentTv.visibility = View.GONE
                                    messageContentTv.text = "데이터 로드 실패"
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            addPostFab.setOnClickListener {
                findNavController().navigate(R.id.createPostFragment)
            }
            circleProfileIv.setOnClickListener {
                findNavController().navigate(R.id.profileEditFragment)
            }
            settingIv.setOnClickListener {
                findNavController().navigate(R.id.settingFragment)
            }
            loadingIconIv.setOnClickListener {
                evacuationMessageViewModel.refreshEvacuationMessage()
            }
        }
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Firestore에서 사용자 정보 로드
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let {
                    with(binding) {
                        // 닉네임 설정
                        nicknameTv.text = it.nickname
                        // 프로필 이미지 설정
                        if (it.profileImageUrl.isNotEmpty()) {
                            Glide.with(this@CommunityFragment)
                                .load(it.profileImageUrl)
                                .placeholder(R.drawable.jikimi_img)
                                .error(R.drawable.ic_launcher_foreground)
                                .circleCrop()
                                .into(circleProfileIv)
                        } else {
                            circleProfileIv.setImageResource(R.drawable.jikimi_img)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "사용자 프로필 로드 실패: ${e.message}")
            }
    }

    private fun setupChipGroup() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChipId = checkedIds[0]
                currentFilter = when (checkedChipId) {
                    R.id.chip_all -> "전체"
                    R.id.chip_emergency -> "긴급"
                    R.id.chip_info -> "정보"
                    R.id.chip_communication -> "소통"
                    R.id.chip_report -> "제보"
                    else -> "전체"
                }
                filterPosts()
            }
        }
    }

    private fun filterPosts() {
        val filteredPosts = if (currentFilter == "전체") {
            allPosts
        } else {
            allPosts.filter { it.category == currentFilter }
        }
        updateRecyclerView(filteredPosts)
    }

    private fun updateRecyclerView(posts: List<Post>) {
        with(binding) {
            if (posts.isEmpty()) {
                emptyTv.visibility = View.VISIBLE
                postRv.visibility = View.GONE
                emptyTv.text = if (currentFilter == "전체") {
                    "게시물이 없습니다"
                } else {
                    "${currentFilter} 카테고리의 게시물이 없습니다"
                }
            } else {
                emptyTv.visibility = View.GONE
                postRv.visibility = View.VISIBLE
                postAdapter.submitList(posts)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}