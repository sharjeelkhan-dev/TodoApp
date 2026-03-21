package com.todoapp.domain.usecase

import com.todoapp.domain.model.UserProfile
import com.todoapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for signing in with email/password or Google.
 */
class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend fun withEmail(email: String, password: String): Result<UserProfile> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        return repository.signInWithEmail(email, password)
    }

    suspend fun withGoogle(idToken: String): Result<UserProfile> {
        return repository.signInWithGoogle(idToken)
    }
}
