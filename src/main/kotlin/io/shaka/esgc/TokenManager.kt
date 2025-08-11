package io.shaka.esgc

import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import java.util.Base64

class TokenManager(
    private val clientId: String,
    private val clientSecret: String,
    private val refreshToken: String? = null,
    private val client: HttpHandler = OkHttp()
) {
    private val refreshResponseLens = Body.auto<TokenRefreshResponse>().toLens()

    fun getValidAccessToken(): String? {
        val currentRefreshToken = refreshToken ?: getEnvProperty("FREEAGENT_REFRESH_TOKEN")
        
        if (currentRefreshToken.isNullOrBlank()) {
            println("No refresh token available. Set FREEAGENT_REFRESH_TOKEN environment variable.")
            return null
        }

        return refreshTokens(currentRefreshToken)?.access_token
    }


    private fun refreshTokens(currentRefreshToken: String): TokenRefreshResponse? {
        return try {
            val credentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
            val request = Request.Companion(Method.POST, "https://api.freeagent.com/v2/token_endpoint")
                .header("Authorization", "Basic $credentials")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=refresh_token&refresh_token=$currentRefreshToken")

            val response = client(request)

            if (response.status.successful) {
                val refreshResponse = refreshResponseLens(response)
                println("Tokens refreshed successfully")
                refreshResponse
            } else {
                println("Token refresh failed: ${response.status} - ${response.bodyString()}")
                null
            }
        } catch (e: Exception) {
            println("Error refreshing tokens: ${e.message}")
            null
        }
    }

}