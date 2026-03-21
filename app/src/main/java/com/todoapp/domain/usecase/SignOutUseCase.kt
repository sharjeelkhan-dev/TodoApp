package com.todoapp.domain.usecase

import com.todoapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for signing out the current user.
 */
class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}
