package com.example.jikimi.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.jikimi.R
import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.Comment
import com.example.jikimi.data.model.dto.Post
import com.example.jikimi.data.model.dto.User
import com.example.jikimi.databinding.FragmentDetailBinding
import com.example.jikimi.presentation.activity.MainActivity
import com.example.jikimi.presentation.adapter.CommentAdapter
import com.example.jikimi.presentation.adapter.PostImageAdapter
import com.example.jikimi.viewmodel.AuthViewModel
import com.example.jikimi.viewmodel.CommentViewModel
import com.example.jikimi.viewmodel.PostViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val postViewModel: PostViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var imageAdapter: PostImageAdapter
    private var postId: String = ""
    private var post: Post? = null

    // 답글 모드 관련 변수
    private var replyToComment: Comment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BottomNavigationView 숨기기
        (activity as? MainActivity)?.hideBottomNavigation()

        // 로그인 상태 확인
        if (!authViewModel.isLoggedIn()) {
            (activity as MainActivity).showToast("로그인이 필요합니다")
            findNavController().navigate(R.id.loginFragment)
            return
        }
        // 게시물 ID 가져오기
        postId = arguments?.getString("postId") ?: ""
        if (postId.isEmpty()) {
            (activity as MainActivity).showToast("게시물을 찾을 수 없습니다")
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupImageViewPager()
        // 게시물 및 댓글 로드
        postViewModel.getPostById(postId)
        commentViewModel.getCommentsByPost(postId)
    }

    private fun setupRecyclerView() {
        val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""
        commentAdapter = CommentAdapter(
            onDeleteClick = { comment ->
                commentViewModel.deleteComment(comment.id, postId)
            },
            onReplyClick = { comment ->
                // 답글 달기 모드 활성화
                setReplyMode(comment)
            },
            currentUserId = currentUserId
        )
        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupImageViewPager() {
        imageAdapter = PostImageAdapter()
        binding.viewPagerImages.adapter = imageAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPagerImages)
    }

    private fun setupObservers() {
        // StateFlow 관찰을 위해 lifecycleScope 사용
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 게시물 상태 관찰
                launch {
                    postViewModel.post.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                post = resource.data
                                post?.let { updatePostUI(it) }
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                (activity as MainActivity).showToast(resource.message ?: "게시물을 불러오는데 실패했습니다")
                                findNavController().navigateUp()
                            }
                            null -> { /* Initial state, do nothing */ }
                        }
                    }
                }

                // 댓글 상태 관찰
                launch {
                    commentViewModel.comments.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                // 이미 게시물 로딩 중이므로 추가 로딩 표시는 하지 않음
                            }
                            is Resource.Success -> {
                                Log.d("PostDetailFragment", "댓글 데이터: ${resource.data}")
                                val comments = resource.data ?: emptyList()

                                if (comments.isEmpty()) {
                                    binding.tvEmptyComments.visibility = View.VISIBLE
                                    binding.rvComments.visibility = View.GONE
                                } else {
                                    binding.tvEmptyComments.visibility = View.GONE
                                    binding.rvComments.visibility = View.VISIBLE
                                    commentAdapter.updateComments(comments)
                                }
                            }
                            is Resource.Error -> {
                                (activity as MainActivity).showToast(resource.message ?: "댓글을 불러오는데 실패했습니다")
                            }
                        }
                    }
                }

                // 댓글 추가 상태 관찰
                launch {
                    commentViewModel.addCommentStatus.collect { resource ->
                        if (resource != null) {
                            when (resource) {
                                is Resource.Loading -> {
                                    binding.btnSendComment.isEnabled = false
                                }
                                is Resource.Success -> {
                                    binding.btnSendComment.isEnabled = true
                                    binding.etComment.text?.clear()
                                    commentViewModel.getCommentsByPost(postId)
                                    // 답글 모드 해제
                                    cancelReplyMode()
                                    // 상태 리셋
                                    commentViewModel.resetAddCommentStatus()
                                }
                                is Resource.Error -> {
                                    binding.btnSendComment.isEnabled = true
                                    (activity as MainActivity).showToast(resource.message ?: "댓글 작성에 실패했습니다")
                                    // 상태 리셋
                                    commentViewModel.resetAddCommentStatus()
                                }
                            }
                        }
                    }
                }

                // 댓글 삭제 상태 관찰
                launch {
                    commentViewModel.deleteCommentStatus.collect { resource ->
                        if (resource != null) {
                            when (resource) {
                                is Resource.Loading -> {
                                    // 로딩 처리
                                }
                                is Resource.Success -> {
                                    (activity as MainActivity).showToast("댓글이 삭제되었습니다")
                                    commentViewModel.getCommentsByPost(postId)
                                    // 상태 리셋
                                    commentViewModel.resetDeleteCommentStatus()
                                }
                                is Resource.Error -> {
                                    (activity as MainActivity).showToast(resource.message ?: "댓글 삭제에 실패했습니다")
                                    // 상태 리셋
                                    commentViewModel.resetDeleteCommentStatus()
                                }
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
                                    findNavController().navigate(R.id.commonsenseFragment)
//                                    (activity as MainActivity).showBottomNavigation()
                                    // 상태 리셋
                                    postViewModel.resetDeletePostStatus()
                                }
                                is Resource.Error -> {
                                    binding.progressBar.visibility = View.GONE
                                    (activity as MainActivity).showToast(resource.message ?: "게시물 삭제에 실패했습니다")
                                    // 상태 리셋
                                    postViewModel.resetDeletePostStatus()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun updatePostUI(post: Post) {
        binding.apply {
            tvNickname.text = post.nickname
            tvContent.text = post.content
            tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
                post.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            tvCategory.text = post.category
            tvCategory.visibility = if (post.category.isNotEmpty()) View.VISIBLE else View.GONE

            // 이미지 처리
            if (post.imageUrls.isNotEmpty()) {
                layoutImages.visibility = View.VISIBLE
                imageAdapter.updateImages(post.imageUrls)
                dotsIndicator.setViewPager2(viewPagerImages)
            } else {
                layoutImages.visibility = View.GONE
            }

            // 사용자 프로필 이미지 로드 추가
            val userId = post.userId
            if (userId.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        user?.let {
                            if (it.profileImageUrl.isNotEmpty()) {
                                Glide.with(requireContext())
                                    .load(it.profileImageUrl)
                                    .placeholder(R.drawable.jikimi_img)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .circleCrop()
                                    .into(ivUserProfile)
                            } else {
                                ivUserProfile.setImageResource(R.drawable.jikimi_img)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PostDetailFragment", "사용자 프로필  이미지 로드 실패: ${e.message}")
                        ivUserProfile.setImageResource(R.drawable.ic_launcher_foreground)
                    }
            } else {
                ivUserProfile.setImageResource(R.drawable.ic_launcher_foreground)
            }

            val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""
            btnDelete.isVisible = post.userId == currentUserId
//            btnEdit.isVisible = post.userId == currentUserId
        }
    }

    private fun setupListeners() {
        binding.btnSendComment.setOnClickListener {
            val commentContent = binding.etComment.text.toString().trim()

            if (commentContent.isEmpty()) {
                (activity as MainActivity).showToast("댓글 내용을 입력하세요")
                return@setOnClickListener
            }

            // 답글 모드인 경우
            if (replyToComment != null) {
                commentViewModel.addComment(postId, commentContent, replyToComment?.id ?: "")
            } else {
                commentViewModel.addComment(postId, commentContent)
            }
        }

        binding.btnDelete.setOnClickListener {
            postViewModel.deletePost(postId)
        }

//        binding.btnEdit.setOnClickListener {
//            // 게시물 수정을 위해 CreatePostFragment로 이동
//            post?.let {
//                val bundle = Bundle().apply {
//                    putString("postId", post?.id)
//                }
//                findNavController().navigate(R.id.createPostFragment, bundle)
//            }
//        }

        // 답글 취소 버튼
        binding.btnCancelReply.setOnClickListener {
            cancelReplyMode()
        }
    }

    // 답글 모드 설정
    private fun setReplyMode(parentComment: Comment) {
        replyToComment = parentComment
        binding.replyModeLayout.visibility = View.VISIBLE
        binding.tvReplyingTo.text = "답글: ${parentComment.nickname}"
        binding.etComment.hint = "${parentComment.nickname}님에게 답글 작성..."
        binding.etComment.requestFocus()

        // 키보드 보이기
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
    }

    // 답글 모드 취소
    private fun cancelReplyMode() {
        replyToComment = null
        binding.replyModeLayout.visibility = View.GONE
        binding.etComment.hint = "댓글을 입력하세요..."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


