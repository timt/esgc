package io.shaka.esgc

import com.fasterxml.jackson.annotation.JsonProperty
import com.stripe.model.BalanceTransaction
import com.stripe.model.Charge
import com.stripe.model.Customer
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

data class FreeAgentStatement(
    @JsonProperty("statement")
    val transactions: List<FreeAgentTransaction>
) {
    val size: Int = transactions.size
}

data class FreeAgentTransaction(
    val dated_on: String, // YYYY-MM-DD format
    val amount: BigDecimal,
    val description: String,
    val fitid: String? = null
)

fun List<Charge>.toFreeAgentStatement(): FreeAgentStatement {
    val transactions = this.flatMap { charge ->
        charge.toFreeAgentTransactions()
    }
    return FreeAgentStatement(transactions)
}

@OptIn(ExperimentalTime::class)
fun Charge.toFreeAgentTransactions(): List<FreeAgentTransaction> {
    val customer = Customer.retrieve(this.customer)
    val date = Instant.fromEpochSeconds(this.created)
    val dateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())
        .format(date.toJavaInstant())
    val amount = BigDecimal(this.amount).movePointLeft(2) // Convert from cents to dollars
    val description = "${this.description} - ${customer.name}"
    val balanceTransaction = BalanceTransaction.retrieve(this.balanceTransaction)
    val stripeFee = BigDecimal(balanceTransaction.fee).movePointLeft(2).negate()

    val transactionRow = FreeAgentTransaction(
        dated_on = dateFormatted,
        amount = amount,
        description = description,
        fitid = this.id // Use Stripe charge ID as unique identifier
    )
    val feeRow = FreeAgentTransaction(
        dated_on = dateFormatted,
        amount = stripeFee,
        description = "Stripe processing fees",
        fitid = "${this.id}_fee"
    )
    val refundRows = if (this.refunded) {
        this.refunds.data.map { refund ->
            val refundDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(Instant.fromEpochSeconds(refund.created).toJavaInstant())
            val refundAmount = BigDecimal(refund.amount).movePointLeft(2).negate() // Refund amount
            val refundDescription = "Refund for ${this.description} - ${customer.name}"
            FreeAgentTransaction(
                dated_on = refundDate,
                amount = refundAmount,
                description = refundDescription,
                fitid = refund.id
            )
        }
    } else {
        emptyList<FreeAgentTransaction>()
    }
    return (listOf(transactionRow, feeRow) + refundRows).sortedBy { it.dated_on }.reversed()
}
