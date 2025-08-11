package io.shaka.esgc

import com.stripe.Stripe
import com.stripe.model.Charge
import com.stripe.param.ChargeListParams
import java.time.LocalDate
import java.time.ZoneId

class StripeClient(apiKey: String) {
    
    init {
        Stripe.apiKey = apiKey
    }
    
    fun fetchChargesFromLastDays(days: Int = 31, limit: Long = 100): List<Charge> {
        val startDate = LocalDate.now().minus(days.toLong(), java.time.temporal.ChronoUnit.DAYS)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().epochSecond
            
        val params = ChargeListParams.builder()
            .setLimit(limit)
            .setCreated(
                ChargeListParams.Created.builder()
                    .setGte(startDate)
                    .build()
            )
            .addExpand("data.refunds")
            .build()

        return Charge.list(params).data
    }
}