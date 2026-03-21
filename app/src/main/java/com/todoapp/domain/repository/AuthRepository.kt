package com.todoapp.domain.repository

import com.todoapp.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Authentication operations.
 */
interface AuthRepository {

    /** Observe current user authentication state. */
    val currentUser: Flow<UserProfile?>

    /** Check if user is currently signed in. */
    val isSignedIn: Boolean

    /** Get the current user's UID. */
    val currentUserId: String?

    /** Sign in with email and password. */
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>

    /** Create account with email and password. */
    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile>

    /** Sign in with Google credential. */
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>

    /** Sign out the current user. */
    suspend fun signOut()
}
