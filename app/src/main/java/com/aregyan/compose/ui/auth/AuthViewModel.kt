package com.aregyan.compose.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aregyan.compose.data.db.VocabulousDatabase
import com.aregyan.compose.repository.AuthRepository
import com.aregyan.compose.ui.auth.model.AuthState
import com.aregyan.compose.ui.auth.model.User
import com.aregyan.compose.util.DataMigrationUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val database: VocabulousDatabase,
    private val dataMigrationUtil: DataMigrationUtil
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Result<Unit>?>(null)
    val resetPasswordState: StateFlow<Result<Unit>?> = _resetPasswordState.asStateFlow()

    // Add a getter for the current user
    val currentUser get() = authRepository.currentUser

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signIn(email, password)
                if (result.isSuccess) {
                    // Check if we need to migrate local data to Firestore
                    handleDataMigration()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error signing in")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signUp(email, password, displayName)
                if (result.isSuccess) {
                    // For new users, we don't need to migrate data
                    // But we should clear any existing local data
                    database.clearAllData()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error signing up")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = authRepository.signInWithGoogle(credential)
                if (result.isSuccess) {
                    // Check if we need to migrate local data to Firestore
                    handleDataMigration()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error signing in with Google")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign in with Google using the new Credential Manager approach
     */
    suspend fun signInWithGoogleCredentialManager(context: Context) {
        _authState.value = AuthState.Loading
        try {
            // Use the GoogleAuthHelper to get the credential
            val googleAuthHelper = GoogleAuthHelper(context)
            val credential = googleAuthHelper.signInWithGoogle()
            
            if (credential != null) {
                // Sign in with the credential
                val result = authRepository.signInWithGoogle(credential)
                if (result.isSuccess) {
                    // Check if we need to migrate local data to Firestore
                    handleDataMigration()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } else {
                _authState.value = AuthState.Error("Failed to get Google credential")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error signing in with Google Credential Manager")
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Clear local database before signing out
                database.clearAllData()
                authRepository.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                Timber.e(e, "Error signing out")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.resetPassword(email)
                _resetPasswordState.value = result
            } catch (e: Exception) {
                Timber.e(e, "Error resetting password")
                _resetPasswordState.value = Result.failure(e)
            }
        }
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = null
    }
    
    fun getUserProfile(userId: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = authRepository.getUserProfile(userId)
                onResult(result.getOrNull())
            } catch (e: Exception) {
                Timber.e(e, "Error getting user profile")
                onResult(null)
            }
        }
    }
    
    /**
     * Handle data migration between local database and Firestore
     * This should be called when a user logs in
     */
    private suspend fun handleDataMigration() {
        try {
            // Check if the user already has data in Firestore
            val hasFirestoreData = dataMigrationUtil.hasFirestoreData()
            
            if (!hasFirestoreData) {
                // If the user doesn't have data in Firestore, migrate local data
                Timber.d("No data found in Firestore, migrating local data")
                dataMigrationUtil.migrateLocalDataToFirestore()
            } else {
                // If the user already has data in Firestore, clear local data
                // to ensure we're using the latest data from Firestore
                Timber.d("Data found in Firestore, clearing local data")
                database.clearAllData()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling data migration")
        }
    }
}
