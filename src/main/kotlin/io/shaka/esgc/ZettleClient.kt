package io.shaka.esgc

import io.shaka.io.shaka.esgc.ZettleTransaction
import org.http4k.client.OkHttp
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.format.Jackson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


fun ZettlePurchase.toZettleTransaction(timestamp: Instant): ZettleTransaction = ZettleTransaction(
    id = this.purchaseUUID,
    timestamp = timestamp,
    amount = this.amountInMajorUnits,
    description = this.description
)

fun List<ZettlePurchase>.asZettleTransactions(tranactions: List<ZettleFinanceTransaction>): List<ZettleTransaction> =
    this.map { val thing = tranactions.find { t -> it.payments.first().uuid == t.originatingTransactionUuid }
        it.toZettleTransaction((thing?.timestamp?:it.timestamp).toInstant()) }

fun ZettleFinanceTransaction.toZettleTransaction(): ZettleTransaction = ZettleTransaction(
    id = this.originatingTransactionUuid,
    timestamp = this.timestamp.toInstant(),
    amount = this.amountInMajorUnits,
    description = this.originatorTransactionType
)

fun List<ZettleFinanceTransaction>.toZettleTransactions(): List<ZettleTransaction> = this.map { it.toZettleTransaction() }

fun String.toInstant(): Instant {
    return OffsetDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).toInstant()
}

class ZettleClient(
    private val tokenManager: ZettleTokenManager,
    private val client: HttpHandler = OkHttp()
) {
    private val baseUrl = "https://purchase.izettle.com"
    private val financeBaseUrl = "https://finance.izettle.com"

    fun getTransactions(days: Int = 10, limit: Int = 50): List<ZettleTransaction> {
        val accessToken = tokenManager.getAccessToken()
        val allTransactions = fetchFinanceTransactionsFromLastDays(days, limit, accessToken)
        val purchases = fetchPurchasesFromLastDays(days, limit, accessToken).asZettleTransactions(allTransactions)
        val financeTransactions = allTransactions
            .filterNot { it.isPayment }
            .toZettleTransactions()
        return (purchases + financeTransactions).sortedBy { it.timestamp }
    }

    fun fetchPurchasesFromLastDays(
        days: Int = 31,
        limit: Int = 100,
        accessToken: String = tokenManager.getAccessToken()
    ): List<ZettlePurchase> {

        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())

        val uri = Uri.of("$baseUrl/purchases/v2")
            .query(
                "startDate=${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}&endDate=${
                    endDate.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                }&limit=$limit&descending=true"
            )

        val response = fetch(uri, accessToken)

        val purchasesResponse = Jackson.asA<ZettlePurchasesResponse>(response.body.stream)
        println("📊 Found ${purchasesResponse.purchases.size} purchases:")

        println("✅ Successfully fetched purchases from Zettle")
        return purchasesResponse.purchases
    }

    fun fetchFinanceTransactionsFromLastDays(
        days: Int = 31,
        limit: Int = 1000,
        accessToken: String = tokenManager.getAccessToken()
    ): List<ZettleFinanceTransaction> {

        val endDateTime = LocalDateTime.now().atZone(ZoneOffset.UTC)
        val startDateTime = endDateTime.minusDays(days.toLong())

        val startFormatted = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss-00:00"))
        val endFormatted = endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss-00:00"))

        val uri = Uri.of("$financeBaseUrl/v2/accounts/liquid/transactions")
            .query("start=$startFormatted&end=$endFormatted&limit=$limit")

        val response = fetch(uri, accessToken)

        val transactions = Jackson.mapper.readValue(
            response.body.stream,
            object : com.fasterxml.jackson.core.type.TypeReference<List<ZettleFinanceTransaction>>() {}
        )

        return transactions
    }

    private fun fetch(uri: Uri, accessToken: String): Response {
        val request = Request(Method.GET, uri)
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        val response = client(request)
        if (response.status != Status.OK) {
            println("❌ Failed to fetch data from Zettle: ${response.status}")
            println("Response: ${response.bodyString()}")
            throw RuntimeException("Failed to fetch data from Zettle: ${response.status}, URI:${uri}, Response: ${response.bodyString()}")
        }
        return response
    }
}