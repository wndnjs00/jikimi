package com.myapp.jikimi.presentation.utils

import android.app.AlertDialog
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DialogUtils {

    companion object {

        // 신고 다이얼로그 표시
        fun showReportDialog(
            context: Context,
            targetId: String,
            targetUserId: String,
            targetNickname: String,
            targetContent: String,
            isPost: Boolean,
            postId: String = "",
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            AlertDialog.Builder(context)
                .setTitle("신고하기")
                .setMessage("정말 신고하시겠어요?")
                .setPositiveButton("신고하기") { _, _ ->
                    if (isPost) {
                        reportPost(
                            targetId,
                            targetUserId,
                            targetNickname,
                            targetContent,
                            onSuccess,
                            onFailure
                        )
                    } else {
                        reportComment(
                            targetId,
                            targetUserId,
                            targetNickname,
                            targetContent,
                            postId,
                            onSuccess,
                            onFailure
                        )
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 차단 다이얼로그 표시
        fun showBlockDialog(
            context: Context,
            targetId: String,
            targetContent: String,
            targetNickname: String,
            isPost: Boolean,
            postId: String = "",
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            AlertDialog.Builder(context)
                .setTitle("차단하기")
                .setMessage("정말 차단하시겠어요?")
                .setPositiveButton("차단하기") { _, _ ->
                    if (isPost) {
                        blockPost(targetId, targetContent, targetNickname, onSuccess, onFailure)
                    } else {
                        blockComment(
                            targetId,
                            targetContent,
                            targetNickname,
                            postId,
                            onSuccess,
                            onFailure
                        )
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 게시물 신고
        private fun reportPost(
            postId: String,
            userId: String,
            nickname: String,
            content: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val currentUser = FirebaseAuth.getInstance().currentUser
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
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e.message ?: "신고 접수에 실패했습니다")
                }
        }

        // 댓글 신고
        private fun reportComment(
            commentId: String,
            userId: String,
            nickname: String,
            content: String,
            postId: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val currentUser = FirebaseAuth.getInstance().currentUser
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
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e.message ?: "신고 접수에 실패했습니다")
                }
        }

        // 게시물 차단
        private fun blockPost(
            postId: String,
            content: String,
            nickname: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val currentUser = FirebaseAuth.getInstance().currentUser
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
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "차단하기에 실패했습니다")
                    }
            } else {
                onFailure("사용자 정보를 찾을 수 없습니다")
            }
        }

        // 댓글 차단
        private fun blockComment(
            commentId: String,
            content: String,
            nickname: String,
            postId: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val currentUser = FirebaseAuth.getInstance().currentUser
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
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "차단하기에 실패했습니다")
                    }
            } else {
                onFailure("사용자 정보를 찾을 수 없습니다")
            }
        }
    }
}