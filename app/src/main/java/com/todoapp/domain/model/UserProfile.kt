package com.todoapp.domain.model

/**
 * Domain model representing an authenticated user.
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)
