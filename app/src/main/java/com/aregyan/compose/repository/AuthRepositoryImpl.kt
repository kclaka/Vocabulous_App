package com.aregyan.compose.repository

import com.aregyan.compose.ui.auth.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser?> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user)
    } catch (e: Exception) {
        Timber.e(e, "Error signing in with email and password")
        Result.failure(e)
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<FirebaseUser?> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { firebaseUser ->
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user profile in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName
            )
            createUserProfile(user)
        }
        Result.success(result.user)
    } catch (e: Exception) {
        Timber.e(e, "Error signing up with email and password")
        Result.failure(e)
    }

    override suspend fun signInWithGoogle(credential: AuthCredential): Result<FirebaseUser?> = try {
        val result = auth.signInWithCredential(credential).await()
        result.user?.let { firebaseUser ->
            // Check if this is a new user
            if (result.additionalUserInfo?.isNewUser == true) {
                // Create user profile in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                createUserProfile(user)
            } else {
                // Update last login time
                firebaseUser.uid.let { userId ->
                    firestore.collection("users").document(userId)
                        .update("lastLoginAt", System.currentTimeMillis()).await()
                }
            }
        }
        Result.success(result.user)
    } catch (e: Exception) {
        Timber.e(e, "Error signing in with Google")
        Result.failure(e)
    }

    override suspend fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error signing out")
        Result.failure(e)
    }

    override suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error resetting password")
        Result.failure(e)
    }

    override suspend fun createUserProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating user profile")
        Result.failure(e)
    }

    override suspend fun getUserProfile(userId: String): Result<User?> = try {
        val document = firestore.collection("users").document(userId).get().await()
        val user = document.toObject(User::class.java)
        Result.success(user)
    } catch (e: Exception) {
        Timber.e(e, "Error getting user profile")
        Result.failure(e)
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating user profile")
        Result.failure(e)
    }
}
