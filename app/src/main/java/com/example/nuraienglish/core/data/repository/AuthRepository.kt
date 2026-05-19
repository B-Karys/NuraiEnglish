package com.example.nuraienglish.core.data.repository

import com.example.nuraienglish.core.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            if (fbUser == null) {
                trySend(null)
            } else {
                trySend(User(uid = fbUser.uid, email = fbUser.email ?: ""))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val isLoggedIn: Boolean get() = auth.currentUser != null
    val isEmailVerified: Boolean get() = auth.currentUser?.isEmailVerified == true
    val currentEmail: String get() = auth.currentUser?.email ?: ""

    suspend fun reloadCurrentUser() = runCatching { auth.currentUser?.reload()?.await() }
    suspend fun resendVerificationEmail() = runCatching { auth.currentUser?.sendEmailVerification()?.await() }

    suspend fun register(email: String, password: String, displayName: String): Result<User> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val fbUser = result.user!!
            val user = User(uid = fbUser.uid, email = email, displayName = displayName)
            // Best-effort: write profile to Firestore
            runCatching { firestore.collection("users").document(fbUser.uid).set(user.toMap()).await() }
            // Firebase sends a verification link to the email — no backend needed
            runCatching { fbUser.sendEmailVerification().await() }
            user
        }

    suspend fun login(email: String, password: String): Result<Unit> =
        runCatching { auth.signInWithEmailAndPassword(email, password).await() }

    suspend fun logout() = auth.signOut()

    suspend fun getUser(uid: String): User? = runCatching {
        val snap = firestore.collection("users").document(uid).get().await()
        snap.toObject(User::class.java)?.copy(uid = uid)
    }.getOrNull()

    suspend fun updateUserLanguage(uid: String, languageCode: String) {
        runCatching {
            firestore.collection("users").document(uid)
                .update("language", languageCode).await()
        }
    }

    private fun User.toMap() = mapOf(
        "uid" to uid,
        "email" to email,
        "displayName" to displayName,
        "language" to language,
        "points" to points,
        "currentLevel" to currentLevel,
        "unlockedLevels" to unlockedLevels,
        "onboardingComplete" to onboardingComplete,
        "isAdmin" to isAdmin
    )
}
