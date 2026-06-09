package com.example.nuraienglish.core.data.repository

import com.example.nuraienglish.core.data.model.User
import com.example.nuraienglish.core.data.model.AdminAccess
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAdminRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentAdminUid: String get() = auth.currentUser?.uid.orEmpty()

    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val registration = firestore.collection("users")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snap?.documents.orEmpty()
                    .mapNotNull { doc -> doc.toObject(User::class.java)?.copy(uid = doc.id) }
                    .sortedWith(compareBy<User> { it.email.lowercase() }.thenBy { it.displayName.lowercase() })
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveUserProfile(user: User) {
        require(user.uid.isNotBlank()) { "User uid is required" }
        val protectedUser = isProtectedOwner(user.uid, user.email)
        val userToSave = if (protectedUser) user.copy(isAdmin = true) else user
        firestore.collection("users").document(user.uid).set(userToSave.toMap()).await()
    }

    suspend fun deleteUserProfile(uid: String) {
        require(uid.isNotBlank()) { "User uid is required" }
        require(!isProtectedOwner(uid)) { "Owner account cannot be deleted" }
        firestore.collection("users").document(uid).delete().await()
    }

    suspend fun resetUserProgress(uid: String) {
        require(uid.isNotBlank()) { "User uid is required" }
        require(!isProtectedOwner(uid)) { "Owner progress cannot be reset here" }
        val userRef = firestore.collection("users").document(uid)
        val progress = userRef.collection("progress").get().await()
        val batch = firestore.batch()
        progress.documents.forEach { batch.delete(it.reference) }
        batch.update(
            userRef,
            mapOf(
                "points" to 0,
                "currentLevel" to "A1",
                "unlockedLevels" to listOf("A1")
            )
        )
        batch.commit().await()
    }

    suspend fun sendPasswordReset(email: String) {
        require(email.isNotBlank()) { "User email is required" }
        auth.sendPasswordResetEmail(email.trim()).await()
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

    private suspend fun isProtectedOwner(uid: String, email: String = ""): Boolean {
        if (AdminAccess.isOwner(email)) return true
        val snap = firestore.collection("users").document(uid).get().await()
        val storedEmail = snap.getString("email").orEmpty()
        return AdminAccess.isOwner(storedEmail)
    }
}
