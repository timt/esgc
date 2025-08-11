package io.shaka.esgc

import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

data class FreeAgentCSVRow @OptIn(ExperimentalTime::class) constructor(
    val date: String,
    val amount: BigDecimal,
    val description: String
)

fun FreeAgentStatement.toFreeAgentCSVRows(): List<FreeAgentCSVRow> {
    return this.transactions.map { it.toFreeAgentCSVRow() }
}

fun FreeAgentTransaction.toFreeAgentCSVRow(): FreeAgentCSVRow {
    val csvDate = LocalDate.parse(this.dated_on)
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val amount = this.amount
    val description = this.description
    return FreeAgentCSVRow(csvDate, amount, description)
}

fun List<FreeAgentCSVRow>.writeCSVToFile(filePath: String) {
    val file = File(filePath)
    file.printWriter().use { writer ->
        this.forEach { row ->
            writer.println("${row.date},${row.amount},${row.description}")
        }
    }
}

