package io.shaka.esgc

import java.time.LocalDateTime

data class OAuthTokens(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int,
    val token_type: String = "bearer",
    val created_at: LocalDateTime = LocalDateTime.now()
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(created_at.plusSeconds(expires_in.toLong() - 60)) // 1 minute buffer
    }
}

data class TokenRefreshRequest(
    val grant_type: String = "refresh_token",
    val refresh_token: String
)

data class TokenRefreshResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val refresh_token_expires_in: Int
)
