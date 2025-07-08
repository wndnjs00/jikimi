package com.myapp.jikimi.data.repository.Auth


import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    // withContext(Dispatchers.IO): I/O 작업(네트워크 요청, 데이터베이스 작업 등)을 백그라운드 스레드에서 실행하도록 지정
    override suspend fun signup(email: String, password: String, nickname: String): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                // 사용자 생성(firebase를 통해 회원가입)
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .await()   // await를 통해 비동기작업이 완료될때까지 대기 / 성공시, authResult에 사용자의 인증정보가 담김
                // 사용자의 uid가져오기 (authResult.user?.uid를 통해 생성된 사용자의 고유 식별자(UID)를 가져옴)
                val uid = authResult.user?.uid
                    ?: return@withContext Resource.Error("회원가입 실패: 사용자 정보를 가져올 수 없습니다.")

                val user = User(uid, email, nickname)
                // firestore에 "users"컬렉션에 사용자정보 저장
                firestore.collection("users").document(uid).set(user)
                    .await()   // Firestore 데이터베이스의 users 컬렉션에 해당 사용자의 uid를 키로 하는 문서를 생성하고,사용자 데이터를 저장 / await()를 사용하여 Firestore 작업이 완료될 때까지 대기

                return@withContext Resource.Success(user)   // 결과반환
            } catch (e: Exception) {
                return@withContext Resource.Error(e.message ?: "회원가입 중 오류가 발생했습니다.")
            }
        }

    override suspend fun login(email: String, password: String): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                // firebase를 통해 로그인 시도
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
                    .await()   // await를 통해 비동기작업이 완료될때까지 대기 / 성공시, authResult에 사용자의 인증정보가 담김
                // 사용자의 uid가져오기 (authResult.user?.uid를 통해 생성된 사용자의 고유 식별자(UID)를 가져옴)
                val uid = authResult.user?.uid
                    ?: return@withContext Resource.Error("로그인 실패: 사용자 정보를 가져올 수 없습니다.")

                // "users" 컬렉션에서 해당 uid와 일치하는값을 가져옴(회원가입한 uid와 일치하는 uid를 가져옴)
                val userSnapshot = firestore.collection("users").document(uid).get().await()
                // toObject(User::class.java)를 사용하여, User데이터클래스로 변환
                val user = userSnapshot.toObject(User::class.java)
                    ?: return@withContext Resource.Error("사용자 정보를 가져올 수 없습니다.")

                return@withContext Resource.Success(user)   // 결과반환
            } catch (e: Exception) {
                return@withContext Resource.Error(e.message ?: "로그인 중 오류가 발생했습니다.")
            }
        }

    // 로그아웃
    override suspend fun logout(): Resource<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            firebaseAuth.signOut()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "로그아웃 중 오류가 발생했습니다.")
        }
    }

    // 회원탈퇴
    override suspend fun deleteAccount(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Resource.Error("로그인이 필요합니다.")

            // Firebase Auth에서 사용자 계정만 삭제
            currentUser.delete().await()

            return@withContext Resource.Success(true)
        } catch (e: Exception) {
            return@withContext Resource.Error(e.message ?: "회원탈퇴 중 오류가 발생했습니다.")
        }
    }


    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(firebaseUser.uid, firebaseUser.email ?: "", "")
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun getUserProfile(userId: String): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val user = userDoc.toObject(User::class.java)
                    ?: return@withContext Resource.Error("사용자 정보를 찾을 수 없습니다.")

                return@withContext Resource.Success(user)
            } catch (e: Exception) {
                return@withContext Resource.Error(e.message ?: "프로필 정보를 불러오는 중 오류가 발생했습니다.")
            }
        }

    override suspend fun updateProfile(nickname: String, imageUri: Uri?): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = firebaseAuth.currentUser
                    ?: return@withContext Resource.Error("로그인이 필요합니다.")

                var profileImageUrl = ""

                // 이미지가 있으면 업로드
                if (imageUri != null) {
                    val storageRef = storage.reference.child("profile_images/${currentUser.uid}")
                    storageRef.putFile(imageUri).await()
                    profileImageUrl = storageRef.downloadUrl.await().toString()
                } else {
                    // 기존 이미지 URL 가져오기
                    val userDoc =
                        firestore.collection("users").document(currentUser.uid).get().await()
                    val user = userDoc.toObject(User::class.java)
                    profileImageUrl = user?.profileImageUrl ?: ""
                }

                // 사용자 프로필 업데이트
                val updatedUser = User(
                    uid = currentUser.uid,
                    email = currentUser.email ?: "",
                    nickname = nickname,
                    profileImageUrl = profileImageUrl
                )

                firestore.collection("users").document(currentUser.uid).set(updatedUser).await()

                // 모든 게시물의 닉네임과 프로필 이미지 업데이트
                val postsSnapshot = firestore.collection("posts")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val batch = firestore.batch()
                for (document in postsSnapshot.documents) {
                    batch.update(document.reference, "nickname", nickname)
                    batch.update(document.reference, "profileImageUrl", profileImageUrl)
                }

                // 모든 댓글의 닉네임과 프로필 이미지 업데이트
                val commentsSnapshot = firestore.collection("comments")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                for (document in commentsSnapshot.documents) {
                    batch.update(document.reference, "nickname", nickname)
                    batch.update(document.reference, "profileImageUrl", profileImageUrl)
                }

                batch.commit().await()

                return@withContext Resource.Success(updatedUser)
            } catch (e: Exception) {
                return@withContext Resource.Error(e.message ?: "프로필 업데이트 중 오류가 발생했습니다.")
            }
        }
}