package com.todoapp.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.todoapp.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication data source.
 * Handles all authentication operations (email, Google).
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    /** Observe the current user as a Flow. */
    val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user?.toUserProfile())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Check if user is signed in. */
    val isSignedIn: Boolean
        get() = auth.currentUser != null

    /** Get the current user's UID. */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    /** Sign in anonymously without credentials. */
    suspend fun signInAnonymously(): Result<UserProfile> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("Anonymous sign in failed: user is null")
            Result.success(user.toUserProfile())
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    /** Sign in with email and password. */
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign in failed: user is null")
            Result.success(user.toUserProfile())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Create a new account with email and password. */
    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign up failed: user is null")
            Result.success(user.toUserProfile())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sign in with Google ID token. */
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign in failed: user is null")
            Result.success(user.toUserProfile())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sign out the current user. */
    fun signOut() {
        auth.signOut()
    }

    /** Map FirebaseUser to domain UserProfile. */
    private fun com.google.firebase.auth.FirebaseUser.toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            email = email ?: "",
            displayName = displayName ?: "",
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous
        )
    }
}
