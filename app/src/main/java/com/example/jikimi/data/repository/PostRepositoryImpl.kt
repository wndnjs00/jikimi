package com.example.jikimi.data.repository

import android.net.Uri
import android.util.Log
import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.Post
import com.example.jikimi.data.model.dto.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage // Firebase Storage 주입 추가
) : PostRepository {

    override suspend fun getAllPosts(): Resource<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = querySnapshot.toObjects(Post::class.java)

            // 차단된 게시물 필터링
            val blockedPostIds = getBlockedPostIds()
            val filteredPosts = posts.filter { post ->
                !blockedPostIds.contains(post.id)
            }

            Resource.Success(filteredPosts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "게시물을 불러오는 중 오류가 발생했습니다.")
        }
    }

    // 차단한 게시물 ID 가져오기
    private suspend fun getBlockedPostIds(): List<String> {
        val currentUser = firebaseAuth.currentUser ?: return emptyList()

        return try {
            val querySnapshot = firestore.collection("blocked_posts")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.getString("postId") }
        } catch (e: Exception) {
            Log.e("PostRepository", "차단된 게시물 ID 가져오기 실패: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPostById(postId: String): Resource<Post> = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = firestore.collection("posts").document(postId).get().await()
            val post = documentSnapshot.toObject(Post::class.java)
                ?: return@withContext Resource.Error("게시물을 찾을 수 없습니다.")

            Resource.Success(post)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "게시물을 불러오는 중 오류가 발생했습니다.")
        }
    }

    override suspend fun createPost(content: String, images: List<Uri>?, category: String): Resource<Post> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Resource.Error("로그인이 필요합니다.")

            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return@withContext Resource.Error("사용자 정보를 찾을 수 없습니다.")

            val postId = firestore.collection("posts").document().id
            val imageUrls = mutableListOf<String>()

            // 이미지가 있으면 업로드
            if (!images.isNullOrEmpty()) {
                for ((index, imageUri) in images.withIndex()) {
                    if (index >= 5) break // 최대 5장으로 제한

                    val imageFileName = "${postId}_image_${index}"
                    val imageRef = storage.reference.child("post_images/$postId/$imageFileName")

                    val uploadTask = imageRef.putFile(imageUri).await()
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    imageUrls.add(downloadUrl)
                }
            }

            val post = Post(
                id = postId,
                userId = currentUser.uid,
                nickname = user.nickname,
                content = content,
                timestamp = System.currentTimeMillis(),
                profileImageUrl = user.profileImageUrl,
                imageUrls = imageUrls,
                category = category
            )

            firestore.collection("posts").document(postId).set(post).await()
            Resource.Success(post)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "게시물 작성 중 오류가 발생했습니다.")
        }
    }

    override suspend fun updatePost(
        postId: String,
        content: String,
        newImages: List<Uri>?,
        existingImages: List<String>?,
        category: String
    ): Resource<Post> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Resource.Error("로그인이 필요합니다.")

            val postDoc = firestore.collection("posts").document(postId).get().await()
            val post = postDoc.toObject(Post::class.java)
                ?: return@withContext Resource.Error("게시물을 찾을 수 없습니다.")

            if (post.userId != currentUser.uid) {
                return@withContext Resource.Error("본인이 작성한 게시물만 수정할 수 있습니다.")
            }

            // 최종 이미지 URL 목록 (기존 이미지 + 새 이미지)
            val imageUrls = mutableListOf<String>()

            // 기존 이미지 중 유지할 이미지
            if (!existingImages.isNullOrEmpty()) {
                imageUrls.addAll(existingImages)
            }

            // 새 이미지가 있으면 업로드
            if (!newImages.isNullOrEmpty()) {
                for ((index, imageUri) in newImages.withIndex()) {
                    if (imageUrls.size + index >= 5) break // 최대 5장으로 제한

                    val imageFileName = "${postId}_image_${System.currentTimeMillis()}_${index}"
                    val imageRef = storage.reference.child("post_images/$postId/$imageFileName")

                    val uploadTask = imageRef.putFile(imageUri).await()
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    imageUrls.add(downloadUrl)
                }
            }

            // 기존 이미지 중 삭제된 이미지 파일 정리
            val oldImageUrls = post.imageUrls
            for (oldUrl in oldImageUrls) {
                if (!imageUrls.contains(oldUrl)) {
                    try {
                        // URL에서 참조 경로 추출 (Firebase Storage URL 형식 가정)
                        val oldRef = storage.getReferenceFromUrl(oldUrl)
                        oldRef.delete().await()
                    } catch (e: Exception) {
                        Log.e("PostRepository", "이미지 삭제 실패: ${e.message}")
                    }
                }
            }

            val updatedPost = post.copy(
                content = content,
                imageUrls = imageUrls,
                category = category
            )

            firestore.collection("posts").document(postId).set(updatedPost).await()

            Resource.Success(updatedPost)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "게시물 수정 중 오류가 발생했습니다.")
        }
    }


    override suspend fun deletePost(postId: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Resource.Error("로그인이 필요합니다.")

            val postDoc = firestore.collection("posts").document(postId).get().await()
            val post = postDoc.toObject(Post::class.java)
                ?: return@withContext Resource.Error("게시물을 찾을 수 없습니다.")

            if (post.userId != currentUser.uid) {
                return@withContext Resource.Error("본인이 작성한 게시물만 삭제할 수 있습니다.")
            }

            // 게시물 삭제
            firestore.collection("posts").document(postId).delete().await()

            // 연관된 댓글들도 삭제
            val commentSnapshots = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()

            val batch = firestore.batch()
            for (document in commentSnapshots.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "게시물 삭제 중 오류가 발생했습니다.")
        }
    }

    override suspend fun getPostsByUser(userId: String): Resource<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = querySnapshot.toObjects(Post::class.java)
            Resource.Success(posts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "사용자 게시물을 불러오는 중 오류가 발생했습니다.")
        }
    }
}