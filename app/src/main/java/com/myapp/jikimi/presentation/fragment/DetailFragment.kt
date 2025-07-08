package com.myapp.jikimi.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.myapp.jikimi.R
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Comment
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.data.model.dto.User
import com.myapp.jikimi.databinding.FragmentDetailBinding
import com.myapp.jikimi.presentation.activity.MainActivity
import com.myapp.jikimi.presentation.adapter.CommentAdapter
import com.myapp.jikimi.presentation.adapter.PostImageAdapter
import com.myapp.jikimi.presentation.utils.DialogUtils
import com.myapp.jikimi.presentation.utils.showToast
import com.myapp.jikimi.viewmodel.AuthViewModel
import com.myapp.jikimi.viewmodel.CommentViewModel
import com.myapp.jikimi.viewmodel.PostViewModel
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
            requireContext().showToast("로그인이 필요합니다")
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // 게시물 ID 가져오기
        postId = arguments?.getString("postId") ?: ""
        if (postId.isEmpty()) {
            requireContext().showToast("게시물을 찾을 수 없습니다")
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
                showCommentOptionsPopup(comment, isUserComment, view)
            },
            onReplyClick = { comment ->
                setReplyMode(comment)
            },
            currentUserId = currentUserId
        )
        binding.commentsRv.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupImageViewPager() {
        imageAdapter = PostImageAdapter()
        with(binding) {
            imagesVp.adapter = imageAdapter
            dotsIndicator.setViewPager2(imagesVp)
        }
    }

    private fun setupObservers() {
        // StateFlow 관찰을 위해 lifecycleScope 사용
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 게시물 상태 관찰
                launch {
                    postViewModel.post.collect { resource ->
                        with(binding) {
                            when (resource) {
                                is Resource.Loading -> {
                                    progressBar.visibility = View.VISIBLE
                                }

                                is Resource.Success -> {
                                    progressBar.visibility = View.GONE
                                    post = resource.data
                                    post?.let { updatePostUI(it) }
                                }

                                is Resource.Error -> {
                                    progressBar.visibility = View.GONE
                                    requireContext().showToast(
                                        resource.message ?: "게시물을 불러오는데 실패했습니다"
                                    )
                                    findNavController().navigateUp()
                                }

                                null -> {}
                                else -> {}
                            }
                        }
                    }
                }

                // 댓글 상태 관찰
                launch {
                    commentViewModel.comments.collect { resource ->
                        with(binding) {
                            when (resource) {
                                is Resource.Success -> {
                                    Log.d("PostDetailFragment", "댓글 데이터: ${resource.data}")
                                    val comments = resource.data ?: emptyList()

                                    if (comments.isEmpty()) {
                                        emptyCommentTv.visibility = View.VISIBLE
                                        commentsRv.visibility = View.GONE
                                    } else {
                                        emptyCommentTv.visibility = View.GONE
                                        commentsRv.visibility = View.VISIBLE
                                        commentAdapter.updateComments(comments)
                                    }
                                }

                                is Resource.Error -> {
                                    requireContext().showToast(
                                        resource.message ?: "댓글을 불러오는데 실패했습니다"
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }

                // 댓글 추가 상태 관찰
                launch {
                    commentViewModel.addCommentStatus.collect { resource ->
                        with(binding) {
                            if (resource != null) {
                                when (resource) {
                                    is Resource.Loading -> {
                                        commentSendBtn.isEnabled = false
                                    }

                                    is Resource.Success -> {
                                        commentSendBtn.isEnabled = true
                                        commentEt.text?.clear()
                                        commentViewModel.getCommentsByPost(postId)
                                        // 답글 모드 해제
                                        cancelReplyMode()
                                        // 상태 리셋
                                        commentViewModel.resetAddCommentStatus()
                                    }

                                    is Resource.Error -> {
                                        commentSendBtn.isEnabled = true
                                        requireContext().showToast(
                                            resource.message ?: "댓글 작성에 실패했습니다"
                                        )
                                        // 상태 리셋
                                        commentViewModel.resetAddCommentStatus()
                                    }

                                    else -> {}
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
                                is Resource.Success -> {
                                    requireContext().showToast("댓글이 삭제되었습니다")
                                    commentViewModel.getCommentsByPost(postId)
                                    // 상태 리셋
                                    commentViewModel.resetDeleteCommentStatus()
                                }

                                is Resource.Error -> {
                                    requireContext().showToast(resource.message ?: "댓글 삭제에 실패했습니다")
                                    // 상태 리셋
                                    commentViewModel.resetDeleteCommentStatus()
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
                                        findNavController().navigate(R.id.communityFragment)
                                        // 상태 리셋
                                        postViewModel.resetDeletePostStatus()
                                    }

                                    is Resource.Error -> {
                                        progressBar.visibility = View.GONE
                                        requireContext().showToast(
                                            resource.message ?: "게시물 삭제에 실패했습니다"
                                        )
                                        // 상태 리셋
                                        postViewModel.resetDeletePostStatus()
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updatePostUI(post: Post) {
        with(binding) {
            nicknameTv.text = post.nickname
            postContentTv.text = post.content
            timeStampTv.text = DateUtils.getRelativeTimeSpanString(
                post.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            categoryTv.text = post.category
            categoryTv.visibility = if (post.category.isNotEmpty()) View.VISIBLE else View.GONE

            // 이미지 처리
            if (post.imageUrls.isNotEmpty()) {
                imageLinear.visibility = View.VISIBLE
                imageAdapter.submitList(post.imageUrls)
                dotsIndicator.setViewPager2(imagesVp)
            } else {
                imageLinear.visibility = View.GONE
            }

            // 사용자 프로필 이미지 로드
            loadUserProfileImage(post.userId)

            // 옵션 버튼 설정
            val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""
            optionsBtn.isVisible = true
            optionsBtn.setOnClickListener {
                showPostOptionsPopup(post, post.userId == currentUserId)
            }
        }
    }

    private fun loadUserProfileImage(userId: String) {
        with(binding) {
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
                                    .into(userProfileCircleIv)
                            } else {
                                userProfileCircleIv.setImageResource(R.drawable.jikimi_img)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PostDetailFragment", "사용자 프로필 이미지 로드 실패: ${e.message}")
                        userProfileCircleIv.setImageResource(R.drawable.ic_launcher_foreground)
                    }
            } else {
                userProfileCircleIv.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            commentSendBtn.setOnClickListener {
                val commentContent = commentEt.text.toString().trim()

                if (commentContent.isEmpty()) {
                    requireContext().showToast("댓글 내용을 입력하세요")
                    return@setOnClickListener
                }

                // 답글 모드인 경우
                if (replyToComment != null) {
                    commentViewModel.addComment(postId, commentContent, replyToComment?.id ?: "")
                } else {
                    commentViewModel.addComment(postId, commentContent)
                }
            }

            // 답글 취소 버튼
            replyCancleBtn.setOnClickListener {
                cancelReplyMode()
            }
        }
    }

    // 답글 모드 설정
    private fun setReplyMode(parentComment: Comment) {
        replyToComment = parentComment
        with(binding) {
            replyLinear.visibility = View.VISIBLE
            replyTv.text = "답글: ${parentComment.nickname}"
            commentEt.hint = "${parentComment.nickname}님에게 답글 작성..."
            commentEt.requestFocus()
            // 키보드 보이기
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(commentEt, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // 답글 모드 취소
    private fun cancelReplyMode() {
        replyToComment = null
        with(binding) {
            replyLinear.visibility = View.GONE
            commentEt.hint = "댓글을 입력하세요..."
        }
    }

    // 게시물 옵션 팝업메뉴 표시
    private fun showPostOptionsPopup(post: Post, isUserPost: Boolean) {
        val popupMenu = PopupMenu(requireContext(), binding.optionsBtn)

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
                        putBoolean("hideBottomNav", true)
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
                    DialogUtils.showReportDialog(
                        context = requireContext(),
                        targetId = post.id,
                        targetUserId = post.userId,
                        targetNickname = post.nickname,
                        targetContent = post.content,
                        isPost = true,
                        onSuccess = {
                            requireContext().showToast("관리자에게 신고가 접수되었습니다")
                        },
                        onFailure = { message ->
                            requireContext().showToast(message)
                        }
                    )
                    true
                }

                4 -> {
                    // 차단하기
                    DialogUtils.showBlockDialog(
                        context = requireContext(),
                        targetId = post.id,
                        targetContent = post.content,
                        targetNickname = post.nickname,
                        isPost = true,
                        onSuccess = {
                            requireContext().showToast("게시물을 차단했습니다")
                            findNavController().navigateUp()
                        },
                        onFailure = { message ->
                            requireContext().showToast(message)
                        }
                    )
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    // 댓글 옵션 팝업메뉴 표시
    private fun showCommentOptionsPopup(
        comment: Comment,
        isUserComment: Boolean,
        anchorView: View
    ) {
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
                    DialogUtils.showReportDialog(
                        context = requireContext(),
                        targetId = comment.id,
                        targetUserId = comment.userId,
                        targetNickname = comment.nickname,
                        targetContent = comment.content,
                        isPost = false,
                        postId = postId,
                        onSuccess = {
                            requireContext().showToast("관리자에게 신고가 접수되었습니다")
                        },
                        onFailure = { message ->
                            requireContext().showToast(message)
                        }
                    )
                    true
                }

                3 -> {
                    // 차단하기
                    DialogUtils.showBlockDialog(
                        context = requireContext(),
                        targetId = comment.id,
                        targetContent = comment.content,
                        targetNickname = comment.nickname,
                        isPost = false,
                        postId = postId,
                        onSuccess = {
                            requireContext().showToast("댓글을 차단했습니다")
                            commentViewModel.getCommentsByPost(postId)
                        },
                        onFailure = { message ->
                            requireContext().showToast(message)
                        }
                    )
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity)?.showBottomNavigation()
    }
}