package io.shaka

import io.shaka.esgc.FreeAgentClient
import io.shaka.esgc.StripeClient
import io.shaka.esgc.TokenManager
import io.shaka.esgc.getEnvProperty
import io.shaka.esgc.toFreeAgentCSVRows
import io.shaka.esgc.toFreeAgentStatement
import io.shaka.esgc.writeCSVToFile

fun main() {
    println("Fetching Stripe transactions...")

    // Load configuration from environment variables
    val stripeApiKey = getEnvProperty("STRIPE_API_KEY")
    val freeAgentClientId = getEnvProperty("FREEAGENT_CLIENT_ID")
    val freeAgentClientSecret = getEnvProperty("FREEAGENT_CLIENT_SECRET")
    val bankAccountId = getEnvProperty("FREEAGENT_BANK_ACCOUNT_ID")

    // Initialize clients
    val stripeClient = StripeClient(stripeApiKey)
    val tokenManager = TokenManager(freeAgentClientId, freeAgentClientSecret)

    // Fetch the list of charges (transactions)
    val charges = stripeClient.fetchChargesFromLastDays(10, 50)

    println("Processing ${charges.size} transactions...")
    
    // Convert to FreeAgent format
    val freeAgentStatement = charges.toFreeAgentStatement()


    // Create FreeAgent client and upload transactions
    val freeAgentClient = FreeAgentClient(tokenManager, bankAccountId)
    freeAgentClient.processStatement(freeAgentStatement)

    // Still create CSV as backup
    val csvRows = freeAgentStatement.toFreeAgentCSVRows()
    csvRows.writeCSVToFile("output.csv")
    println("Backup CSV file written to output.csv")
}






