package com.example.jikimi.presentation.fragment

import android.os.Bundle
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
import com.example.jikimi.R
import com.example.jikimi.Resource
import com.example.jikimi.databinding.FragmentCommunityBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.presentation.adapter.PostAdapter
import com.example.jikimi.viewmodel.AuthViewModel
import com.example.jikimi.viewmodel.PostViewModel
import com.example.jikimi.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommunityFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentCommunityBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val postViewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNickname()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        // 게시물 목록 불러오기
        postViewModel.getPosts()
    }


    // sharedViewModel로 닉네임데이터 관찰해서 표시
    private fun setupNickname() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.userNickname.collect { nickname ->
                    if (nickname != null) {
                        binding.tvNickname.text = nickname
                    }
                }
            }
        }
    }


    private fun setupRecyclerView() {
        val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""

        postAdapter = PostAdapter(
            onPostClick = { post ->
                val bundle = Bundle().apply {
                    putString("postId", post.id)
                }
                findNavController().navigate(R.id.detailFragment, bundle)
            },
            onDeleteClick = { post ->
                postViewModel.deletePost(post.id)
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
//                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                val posts = resource.data ?: emptyList()

                                if (posts.isEmpty()) {
                                    binding.tvEmpty.visibility = View.VISIBLE
                                    binding.rvPosts.visibility = View.GONE
                                } else {
                                    binding.tvEmpty.visibility = View.GONE
                                    binding.rvPosts.visibility = View.VISIBLE
                                    postAdapter.updatePosts(posts)
                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                (activity as MainActivity).showToast(resource.message ?: "게시물을 불러오는데 실패했습니다")
                            }
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
                            }
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
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}