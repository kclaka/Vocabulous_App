package com.aregyan.compose.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.aregyan.compose.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Helper class for Google Sign-In using the Credential Manager API
 */
class GoogleAuthHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    
    /**
     * Initiates the Google Sign-In flow using Credential Manager
     * @return AuthCredential if successful, null otherwise
     */
    suspend fun signInWithGoogle(): AuthCredential? {
        try {
            // Create a Google ID token request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false) // Show all Google accounts on the device
                .setNonce(null) // Optional nonce to prevent replay attacks
                .build()
            
            // Create the credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            // Show the credential picker and wait for user selection
            val response = credentialManager.getCredential(context, request)
            
            // Handle the credential response
            return handleSignInResponse(response)
        } catch (e: GetCredentialException) {
            Timber.e(e, "Error getting Google credential")
            return null
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during Google Sign-In")
            return null
        }
    }
    
    /**
     * Processes the credential response and converts it to a Firebase AuthCredential
     */
    private fun handleSignInResponse(response: GetCredentialResponse): AuthCredential? {
        // Check if the response contains a Google ID token
        val credential = response.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                // Parse the credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                
                // Get the ID token
                val idToken = googleIdTokenCredential.idToken
                
                // Create a Firebase credential from the Google ID token
                return GoogleAuthProvider.getCredential(idToken, null)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing Google ID token credential")
                return null
            }
        }
        return null
    }
}
