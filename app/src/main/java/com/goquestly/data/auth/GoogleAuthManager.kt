package com.goquestly.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(webClientId: String): Result<GoogleSignInResult> {
        return try {
            Log.d(TAG, "Starting Google Sign-In")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Sign-in cancelled by user")
            Result.failure(GoogleSignInCancelledException())
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No Google account available", e)
            Result.failure(Exception("No Google account found"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign-in failed", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            Log.d(TAG, "Sign-in cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign-in", e)
            Result.failure(e)
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): Result<GoogleSignInResult> {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        val email = googleIdTokenCredential.id
                        val displayName = googleIdTokenCredential.displayName
                        val profilePictureUri =
                            googleIdTokenCredential.profilePictureUri?.toString()

                        Log.d(TAG, "Sign-in successful")

                        Result.success(
                            GoogleSignInResult(
                                idToken = idToken,
                                email = email,
                                displayName = displayName,
                                profilePictureUri = profilePictureUri
                            )
                        )
                    } else {
                        Log.e(TAG, "Unexpected credential type: ${credential.type}")
                        Result.failure(Exception("Unexpected credential type"))
                    }
                }

                else -> {
                    Log.e(TAG, "Unexpected credential type")
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Failed to parse Google ID token", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
    }
}

data class GoogleSignInResult(
    val idToken: String,
    val email: String,
    val displayName: String?,
    val profilePictureUri: String?
)

class GoogleSignInCancelledException : Exception("User cancelled Google Sign-In")
