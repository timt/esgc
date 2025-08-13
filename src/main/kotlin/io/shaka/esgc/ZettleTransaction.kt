package io.shaka.io.shaka.esgc

import io.shaka.esgc.FreeAgentStatement
import io.shaka.esgc.FreeAgentTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

data class ZettleTransaction @OptIn(ExperimentalTime::class) constructor(
    val id: String?,
    val timestamp: Instant,
    val amount: BigDecimal,
    val description: String,
)

val instantToFreeAgentDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

fun ZettleTransaction.toFreeAgentTransaction(): FreeAgentTransaction {
    return FreeAgentTransaction(
        dated_on = instantToFreeAgentDateFormatter.format(timestamp),
        amount = amount,
        description = description,
        fitid = id
    )
}

fun List<ZettleTransaction>.toFreeAgentStatement(): FreeAgentStatement {
    val transactions = this.map { it.toFreeAgentTransaction() }
    return FreeAgentStatement(transactions)
}