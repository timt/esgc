package io.shaka

import io.shaka.esgc.FreeAgentClient
import io.shaka.esgc.TokenManager
import io.shaka.esgc.ZettleClient
import io.shaka.esgc.ZettleTokenManager
import io.shaka.esgc.getEnvProperty
import io.shaka.io.shaka.esgc.toFreeAgentStatement

fun main() {
    println("Fetching Zettle transactions...")

    val zettleApiKey = getEnvProperty("ZETTLE_API_KEY")
    val zettleClientId = getEnvProperty("ZETTLE_CLIENT_ID")
    val freeAgentClientId = getEnvProperty("FREEAGENT_CLIENT_ID")
    val freeAgentClientSecret = getEnvProperty("FREEAGENT_CLIENT_SECRET")
    val bankAccountId = getEnvProperty("FREEAGENT_BANK_ACCOUNT_ID")

    val zettleClient = ZettleClient(ZettleTokenManager(zettleClientId, zettleApiKey))
    val freeAgentClient = FreeAgentClient(TokenManager(freeAgentClientId, freeAgentClientSecret), bankAccountId)

    try {
        val transactions = zettleClient.getTransactions()
        val freeAgentStatement = transactions.toFreeAgentStatement()
        freeAgentClient.processStatement(freeAgentStatement)
    } catch (e: Exception) {
        println("❌ Error fetching Zettle transactions: ${e.message}")
        e.printStackTrace()
    }
}
