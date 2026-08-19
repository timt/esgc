package io.shaka.esgc

import com.stripe.model.Charge
import kotlin.test.Test
import kotlin.test.assertEquals

class StripeClientTest {

    @Test
    fun `keeps only succeeded captured charges`() {
        val succeeded = charge("ch_succeeded", status = "succeeded", captured = true)
        val failed = charge("ch_failed", status = "failed", captured = false)
        val pending = charge("ch_pending", status = "pending", captured = false)
        val uncaptured = charge("ch_uncaptured", status = "succeeded", captured = false)

        val result = listOf(succeeded, failed, pending, uncaptured).settledOnly()

        assertEquals(listOf(succeeded), result)
    }

    @Test
    fun `returns empty list when no charges settled`() {
        val result = listOf(charge("ch_failed", status = "failed", captured = false)).settledOnly()

        assertEquals(emptyList(), result)
    }

    private fun charge(id: String, status: String, captured: Boolean) = Charge().apply {
        this.id = id
        this.status = status
        this.captured = captured
    }
}
