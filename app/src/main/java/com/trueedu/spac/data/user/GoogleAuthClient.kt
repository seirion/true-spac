package com.trueedu.spac.data.user

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userCycle: UserCycle,
) {

    suspend fun signIn(activityContext: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(activityContext)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                signInWithFirebase(googleIdToken.idToken)
            } else {
                Result.failure(kotlin.Exception("Invalid credential type"))
            }
        } catch (e: Exception) {
            logE("Sign in failed", e)
            Result.failure(e)
        }
    }

    private suspend fun signInWithFirebase(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                val email = user.email ?: ""
                val profileImageUrl = user.photoUrl?.toString() ?: ""
                logD("Login with email: $email, photoUrl: $profileImageUrl")
                userCycle.login(email, profileImageUrl)
                Result.success(user)
            } else {
                Result.failure(Exception("Firebase user is null"))
            }
        } catch (e: Exception) {
            logE("Firebase sign in failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        userCycle.logout()
    }

    /**
     * 계정 삭제
     * Firebase Authentication에서 사용자 계정을 완전히 삭제합니다.
     * 삭제 성공 시 로컬 데이터도 함께 정리됩니다.
     *
     * @return Result<Unit> 성공 시 success, 실패 시 failure with exception
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                logE("계정 삭제 실패: 현재 로그인된 사용자가 없습니다")
                return Result.failure(Exception("No user is currently signed in"))
            }

            logD("계정 삭제 시작: ${user.email}")

            // Firebase에서 계정 삭제
            user.delete().await()

            // 로컬 데이터 정리
            userCycle.logout()

            logD("✅ 계정 삭제 완료")
            Result.success(Unit)
        } catch (e: Exception) {
            logE("❌ 계정 삭제 실패", e)
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}
