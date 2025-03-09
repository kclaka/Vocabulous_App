package com.aregyan.compose.repository

import com.aregyan.compose.ui.auth.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    val currentUserFlow: Flow<FirebaseUser?>
    
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?>
    suspend fun signUp(email: String, password: String, displayName: String): Result<FirebaseUser?>
    suspend fun signInWithGoogle(credential: AuthCredential): Result<FirebaseUser?>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<User?>
    suspend fun updateUserProfile(user: User): Result<Unit>
}
