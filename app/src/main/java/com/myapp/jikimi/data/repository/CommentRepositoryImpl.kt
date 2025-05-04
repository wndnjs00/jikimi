package com.myapp.jikimi.data.repository

import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Comment
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.data.model.dto.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CommentRepository {

    override suspend fun getCommentsByPost(postId: String): Resource<List<Comment>> = withContext(
        Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val comments = querySnapshot.toObjects(Comment::class.java)
            Resource.Success(comments)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "댓글을 불러오는 중 오류가 발생했습니다.")
        }
    }

    override suspend fun addComment(postId: String, content: String, parentCommentId: String): Resource<Comment> = withContext(
        Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Resource.Error("로그인이 필요합니다.")

            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return@withContext Resource.Error("사용자 정보를 찾을 수 없습니다.")

            val commentId = firestore.collection("comments").document().id
            val comment = Comment(
                id = commentId,
                postId = postId,
                userId = currentUser.uid,
                nickname = user.nickname,
                content = content,
                timestamp = System.currentTimeMillis(),
                profileImageUrl = user.profileImageUrl,
                parentCommentId = parentCommentId,
                isReply = parentCommentId.isNotEmpty()
            )

            firestore.collection("comments").document(commentId).set(comment).await()

            // 게시물의 댓글 카운트 증가
            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val postSnapshot = transaction.get(postRef)
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    val newCommentCount = post.commentCount + 1
                    transaction.update(postRef, "commentCount", newCommentCount)
                }
            }.await()

            Resource.Success(comment)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "댓글 작성 중 오류가 발생했습니다.")
        }
    }

    override suspend fun deleteComment(commentId: String, postId: String): Resource<Boolean> = withContext(
        Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Resource.Error("로그인이 필요합니다.")

            val commentDoc = firestore.collection("comments").document(commentId).get().await()
            val comment = commentDoc.toObject(Comment::class.java)
                ?: return@withContext Resource.Error("댓글을 찾을 수 없습니다.")

            if (comment.userId != currentUser.uid) {
                return@withContext Resource.Error("본인이 작성한 댓글만 삭제할 수 있습니다.")
            }

            // 대댓글 삭제
            val replyQuerySnapshot = firestore.collection("comments")
                .whereEqualTo("parentCommentId", commentId)
                .get()
                .await()

            val batch = firestore.batch()
            batch.delete(firestore.collection("comments").document(commentId))

            // 부모 댓글을 삭제할 경우 답글도 모두 삭제
            for (doc in replyQuerySnapshot.documents) {
                batch.delete(doc.reference)
            }

            batch.commit().await()

            // 게시물의 댓글 카운트 감소 (대댓글 포함)
            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val postSnapshot = transaction.get(postRef)
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    // 본 댓글 + 대댓글 수만큼 감소
                    val decreaseCount = 1 + replyQuerySnapshot.size()
                    val newCommentCount = (post.commentCount - decreaseCount).coerceAtLeast(0)
                    transaction.update(postRef, "commentCount", newCommentCount)
                }
            }.await()

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "댓글 삭제 중 오류가 발생했습니다.")
        }
    }
}
