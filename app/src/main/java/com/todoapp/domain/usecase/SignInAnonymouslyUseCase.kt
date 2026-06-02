package com.todoapp.domain.usecase

import com.todoapp.domain.model.UserProfile
import com.todoapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to sign in anonymously.
 */
class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return authRepository.signInAnonymously()
    }
}
