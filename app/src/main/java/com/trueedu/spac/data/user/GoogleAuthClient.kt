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
     * 재인증
     * 보안에 민감한 작업(계정 삭제 등)을 수행하기 전에 사용자를 재인증합니다.
     *
     * @param activityContext Activity context for credential manager
     * @return Result<Unit> 성공 시 success, 실패 시 failure with exception
     */
    private suspend fun reauthenticate(activityContext: Context): Result<Unit> {
        return try {
            logD("재인증 시작")
            val signInResult = signIn(activityContext)
            if (signInResult.isSuccess) {
                logD("재인증 성공")
                Result.success(Unit)
            } else {
                logE("재인증 실패")
                Result.failure(signInResult.exceptionOrNull() ?: Exception("Reauthentication failed"))
            }
        } catch (e: Exception) {
            logE("재인증 중 오류 발생", e)
            Result.failure(e)
        }
    }

    /**
     * 계정 삭제
     * Firebase Authentication에서 사용자 계정을 완전히 삭제합니다.
     * 삭제 성공 시 로컬 데이터도 함께 정리됩니다.
     * 재인증이 필요한 경우 자동으로 재인증을 시도합니다.
     *
     * @param activityContext Activity context (재인증이 필요한 경우 사용)
     * @return Result<Unit> 성공 시 success, 실패 시 failure with exception
     */
    suspend fun deleteAccount(activityContext: Context): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                logE("계정 삭제 실패: 현재 로그인된 사용자가 없습니다")
                return Result.failure(Exception("No user is currently signed in"))
            }

            logD("계정 삭제 시작: ${user.email}")

            try {
                // Firebase에서 계정 삭제
                user.delete().await()
            } catch (e: Exception) {
                // 재인증이 필요한 경우 재인증 후 다시 시도
                if (e.message?.contains("requires recent authentication") == true ||
                    e.message?.contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") == true) {
                    logD("재인증 필요 - 재인증 후 재시도")
                    val reauthResult = reauthenticate(activityContext)
                    if (reauthResult.isFailure) {
                        return Result.failure(reauthResult.exceptionOrNull() ?: e)
                    }
                    // 재인증 성공 후 다시 삭제 시도
                    firebaseAuth.currentUser?.delete()?.await()
                        ?: return Result.failure(Exception("User is null after reauthentication"))
                } else {
                    throw e
                }
            }

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
