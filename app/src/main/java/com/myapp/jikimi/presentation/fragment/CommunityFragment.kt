package com.myapp.jikimi.presentation.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.User
import com.myapp.jikimi.databinding.FragmentCommunityBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.presentation.adapter.PostAdapter
import com.myapp.jikimi.viewmodel.AuthViewModel
import com.myapp.jikimi.viewmodel.EvacuationMessageViewModel
import com.myapp.jikimi.viewmodel.PostViewModel
import com.myapp.jikimi.viewmodel.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.myapp.jikimi.data.model.dto.Post
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

        binding.rvPosts.apply {
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
                        when (resource) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                allPosts = resource.data ?: emptyList()
                                filterPosts() // 필터링 적용
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                (activity as MainActivity).showToast(resource.message ?: "게시물을 불러오는데 실패했습니다")
                            }

                            else -> {}
                        }
                    }
                }

                // 게시물 삭제 상태 관찰
                launch {
                    postViewModel.deletePostStatus.collect { resource ->
                        if (resource != null) {
                            when (resource) {
                                is Resource.Loading -> {
                                    binding.progressBar.visibility = View.VISIBLE
                                }
                                is Resource.Success -> {
                                    binding.progressBar.visibility = View.GONE
                                    (activity as MainActivity).showToast("게시물이 삭제되었습니다")
                                    postViewModel.getPosts() // 목록 새로고침
                                    postViewModel.resetDeletePostStatus() // 상태 리셋
                                }
                                is Resource.Error -> {
                                    binding.progressBar.visibility = View.GONE
                                    (activity as MainActivity).showToast(resource.message ?: "게시물 삭제에 실패했습니다")
                                    postViewModel.resetDeletePostStatus() // 상태 리셋
                                }

                                else -> {}
                            }
                        }
                    }
                }

                // 재난 안내문자 상태 관찰
                launch {
                    evacuationMessageViewModel.evacuationMessage.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.dateContent.visibility = View.GONE
                                binding.messageContent.text = "로딩 중..."
                            }
                            is Resource.Success -> {
                                val message = resource.data
                                if (message != null) {
                                    binding.dateContent.visibility = View.VISIBLE
                                    binding.dateContent.text = message.createdDateTime
                                    binding.messageContent.text = message.messageContent
                                } else {
                                    binding.dateContent.visibility = View.GONE
                                    binding.messageContent.text = "최근 발령된 재난문자가 없습니다"
                                }
                            }
                            is Resource.Error -> {
                                binding.dateContent.visibility = View.GONE
                                binding.messageContent.text = "데이터 로드 실패"
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {

        with(binding){
            fabAddPost.setOnClickListener {
                findNavController().navigate(R.id.createPostFragment)
            }
            ivProfile.setOnClickListener {
                findNavController().navigate(R.id.profileEditFragment)
            }

            // 로딩아이콘 클릭 시 새로고침 기능 추가
            loadingIcon.setOnClickListener {
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
                    // 닉네임 설정
                    binding.tvNickname.text = it.nickname

                    // 프로필 이미지 설정
                    if (it.profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(it.profileImageUrl)
                            .placeholder(R.drawable.jikimi_img)
                            .error(R.drawable.ic_launcher_foreground)
                            .circleCrop()
                            .into(binding.ivProfile)
                    } else {
                        binding.ivProfile.setImageResource(R.drawable.jikimi_img)
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
                    R.id.chipAll -> "전체"
                    R.id.chipEmergency -> "긴급"
                    R.id.chipInfo -> "정보"
                    R.id.chipCommunication -> "소통"
                    R.id.chipReport -> "제보"
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
        if (posts.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvPosts.visibility = View.GONE
            binding.tvEmpty.text = if (currentFilter == "전체") {
                "게시물이 없습니다"
            } else {
                "${currentFilter} 카테고리의 게시물이 없습니다"
            }
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvPosts.visibility = View.VISIBLE
            postAdapter.submitList(posts)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}