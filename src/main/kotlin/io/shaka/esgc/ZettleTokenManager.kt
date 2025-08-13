package io.shaka.esgc

import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.format.Jackson.auto

class ZettleTokenManager(
    private val clientId: String,
    private val apiKey: String, // JWT assertion
    private val client: HttpHandler = OkHttp()
) {
    private val tokenResponseLens = Body.auto<ZettleTokenResponse>().toLens()

    fun getAccessToken(): String = try {
        val request = Request(Method.POST, "https://oauth.zettle.com/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body("grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&client_id=$clientId&assertion=$apiKey")

        val response = client(request)

        if (response.status == Status.OK) {
            val tokenResponse = tokenResponseLens(response)
            println("✅ Successfully obtained Zettle access token (expires in ${tokenResponse.expires_in} seconds)")
            tokenResponse.access_token
        } else {
            println("❌ Failed to get Zettle access token: ${response.status}")
            println("Response: ${response.bodyString()}")
            throw RuntimeException("Failed to get Zettle access token: ${response.status}")
        }
    } catch (e: Exception) {
        println("❌ Error getting Zettle access token: ${e.message}")
        throw e
    }
}

data class ZettleTokenResponse(
    val access_token: String,
    val token_type: String = "Bearer",
    val expires_in: Int,
    val scope: String? = null
)