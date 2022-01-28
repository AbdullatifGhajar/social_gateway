package com.socialgateway.socialgateway.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val id: String,
    val email: String,
    val displayName: String
)