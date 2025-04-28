package com.example.jikimi.presentation.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
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
            onOptionsClick = { comment, isUserComment, view ->
                // view 파라미터 추가 - 클릭된 실제 버튼을 전달받음
                showCommentOptionsPopup(comment, isUserComment, view)
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
            btnOption.isVisible = true // 항상 표시하도록 변경
            btnOption.setOnClickListener {
                if (post.userId == currentUserId) {
                    // 내 게시물인 경우: 수정, 삭제 옵션
                    showPostOptionsPopup(post, true)
                } else {
                    // 타인 게시물인 경우: 신고, 차단 옵션
                    showPostOptionsPopup(post, false)
                }
            }
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

        binding.btnOption.setOnClickListener {
            postViewModel.deletePost(postId)
        }

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

    // 게시물 옵션 팝업메뉴 표시
    private fun showPostOptionsPopup(post: Post, isUserPost: Boolean) {
        val popupMenu = PopupMenu(requireContext(), binding.btnOption)

        if (isUserPost) {
            // 내 게시물인 경우
            popupMenu.menu.add(Menu.NONE, 1, Menu.NONE, "수정하기")
            popupMenu.menu.add(Menu.NONE, 2, Menu.NONE, "삭제하기")
        } else {
            // 타인 게시물인 경우
            popupMenu.menu.add(Menu.NONE, 3, Menu.NONE, "신고하기")
            popupMenu.menu.add(Menu.NONE, 4, Menu.NONE, "차단하기")
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    // 수정하기
                    val bundle = Bundle().apply {
                        putString("postId", post.id)
                    }

                    findNavController().navigate(R.id.createPostFragment, bundle)
                    true
                }
                2 -> {
                    // 삭제하기
                    postViewModel.deletePost(post.id)
                    true
                }
                3 -> {
                    // 신고하기
                    showReportDialog(post.id, post.userId, post.nickname, post.content, true)
                    true
                }
                4 -> {
                    // 차단하기
                    showBlockDialog(post.id, post.content, post.nickname, true)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    // 댓글 옵션 팝업메뉴 표시 - 매개변수 추가: View anchorView
    private fun showCommentOptionsPopup(comment: Comment, isUserComment: Boolean, anchorView: View) {
        // 매개변수로 전달된 anchorView를 사용하여 팝업 메뉴 표시
        val popupMenu = PopupMenu(requireContext(), anchorView)

        if (isUserComment) {
            // 내 댓글인 경우
            popupMenu.menu.add(Menu.NONE, 1, Menu.NONE, "삭제하기")
        } else {
            // 타인 댓글인 경우
            popupMenu.menu.add(Menu.NONE, 2, Menu.NONE, "신고하기")
            popupMenu.menu.add(Menu.NONE, 3, Menu.NONE, "차단하기")
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    // 삭제하기
                    commentViewModel.deleteComment(comment.id, postId)
                    true
                }
                2 -> {
                    // 신고하기
                    showReportDialog(comment.id, comment.userId, comment.nickname, comment.content, false)
                    true
                }
                3 -> {
                    // 차단하기
                    showBlockDialog(comment.id, comment.content, comment.nickname, false)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }


    // 신고 확인 다이얼로그 표시
    private fun showReportDialog(id: String, userId: String, nickname: String, content: String, isPost: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("신고하기")
            .setMessage("정말 신고하시겠어요?")
            .setPositiveButton("신고하기") { _, _ ->
                if (isPost) {
                    reportPost(id, userId, nickname, content)
                } else {
                    reportComment(id, userId, nickname, content)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 차단 확인 다이얼로그 표시
    private fun showBlockDialog(id: String, content: String, nickname: String, isPost: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("차단하기")
            .setMessage("정말 차단하시겠어요?")
            .setPositiveButton("차단하기") { _, _ ->
                if (isPost) {
                    blockPost(id, content, nickname)
                } else {
                    blockComment(id, content, nickname)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 게시물 신고
    private fun reportPost(postId: String, userId: String, nickname: String, content: String) {
        val currentUser = authViewModel.getCurrentUser()
        val report = hashMapOf(
            "reporterId" to (currentUser?.uid ?: ""),
            "postId" to postId,
            "userId" to userId,
            "nickname" to nickname,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("post_reports")
            .add(report)
            .addOnSuccessListener {
                (activity as MainActivity).showToast("관리자에게 신고가 접수되었습니다")
            }
            .addOnFailureListener { e ->
                (activity as MainActivity).showToast("신고 접수에 실패했습니다: ${e.message}")
            }
    }

    // 댓글 신고
    private fun reportComment(commentId: String, userId: String, nickname: String, content: String) {
        val currentUser = authViewModel.getCurrentUser()
        val report = hashMapOf(
            "reporterId" to (currentUser?.uid ?: ""),
            "commentId" to commentId,
            "postId" to postId,
            "userId" to userId,
            "nickname" to nickname,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("comment_reports")
            .add(report)
            .addOnSuccessListener {
                (activity as MainActivity).showToast("관리자에게 신고가 접수되었습니다")
            }
            .addOnFailureListener { e ->
                (activity as MainActivity).showToast("신고 접수에 실패했습니다: ${e.message}")
            }
    }

    // 게시물 차단 (사용자에게만 해당 게시물 숨기기)
    private fun blockPost(postId: String, content: String, nickname: String) {
        val currentUser = authViewModel.getCurrentUser()
        if (currentUser != null) {
            val userId = currentUser.uid
            val blockedPost = hashMapOf(
                "userId" to userId,
                "postId" to postId,
                "content" to content,
                "nickname" to nickname,
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance().collection("blocked_posts")
                .add(blockedPost)
                .addOnSuccessListener {
                    (activity as MainActivity).showToast("게시물을 차단했습니다")
                    findNavController().navigateUp() // 목록으로 돌아가기
                }
                .addOnFailureListener { e ->
                    (activity as MainActivity).showToast("차단하기에 실패했습니다: ${e.message}")
                }
        }
    }

    // 댓글 차단 (사용자에게만 해당 댓글 숨기기)
    private fun blockComment(commentId: String, content: String, nickname: String,) {
        val currentUser = authViewModel.getCurrentUser()
        if (currentUser != null) {
            val userId = currentUser.uid
            val blockedComment = hashMapOf(
                "userId" to userId,
                "commentId" to commentId,
                "content" to content,
                "postId" to postId,
                "nickname" to nickname,
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance().collection("blocked_comments")
                .add(blockedComment)
                .addOnSuccessListener {
                    (activity as MainActivity).showToast("댓글을 차단했습니다")
                    commentViewModel.getCommentsByPost(postId) // 댓글 목록 새로고침
                }
                .addOnFailureListener { e ->
                    (activity as MainActivity).showToast("차단하기에 실패했습니다: ${e.message}")
                }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        (activity as MainActivity)?.showBottomNavigation()
    }
}


