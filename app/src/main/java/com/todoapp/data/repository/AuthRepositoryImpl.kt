package com.todoapp.data.repository

import com.todoapp.data.remote.FirebaseAuthDataSource
import com.todoapp.domain.model.UserProfile
import com.todoapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository.
 * Delegates all operations to FirebaseAuthDataSource.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override val currentUser: Flow<UserProfile?>
        get() = authDataSource.currentUser

    override val isSignedIn: Boolean
        get() = authDataSource.isSignedIn

    override val currentUserId: String?
        get() = authDataSource.currentUserId

    override suspend fun signInAnonymously(): Result<UserProfile> {
        return authDataSource.signInAnonymously()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        return authDataSource.signInWithEmail(email, password)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile> {
        return authDataSource.signUpWithEmail(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> {
        return authDataSource.signInWithGoogle(idToken)
    }

    override suspend fun signOut() {
        authDataSource.signOut()
    }
}
