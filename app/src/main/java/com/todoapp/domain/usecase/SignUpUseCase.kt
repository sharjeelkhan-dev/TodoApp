package com.todoapp.domain.usecase

import com.todoapp.domain.model.UserProfile
import com.todoapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for creating a new account with email/password.
 */
class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return repository.signUpWithEmail(email, password)
    }
}
