package io.shaka.esgc

import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto

class FreeAgentClient(
    private val tokenManager: TokenManager,
    private val bankAccountId: String,
    private val client: HttpHandler = OkHttp()
) {
    private val baseUrl = "https://api.freeagent.com/v2"
    private val statementLens = Body.auto<FreeAgentStatement>().toLens()

    fun createBankTransactions(statement: FreeAgentStatement): Response {
        val accessToken = tokenManager.getValidAccessToken()
            ?: return Response(Status.UNAUTHORIZED).body("Failed to get valid access token")
//        https://api.freeagent.com/v2/bank_transactions/statement?bank_account=:bank_account
        val uri = "$baseUrl/bank_transactions/statement?bank_account=$bankAccountId"
        val request = Request(POST, uri)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .with(statementLens of statement)

        val response = client(request)

        // Retry once if token expired
        return if (response.status == Status.UNAUTHORIZED) {
            println("Access token expired, attempting refresh...")
            val newToken = tokenManager.getValidAccessToken()
            if (newToken != null) {
                val retryRequest = request
                    Request(POST, uri)
                    .header("Authorization", "Bearer $newToken")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .with(statementLens of statement)
                client(retryRequest)
            } else {
                response
            }
        } else {
            response
        }
    }
}