package hr.kotwave.scorpiongym.membershiprecord

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

/**
 * Tests for `chooseRenewalStartDate` — the pure function that decides whether
 * a new MembershipRecord should start today or chain onto the latest unexpired
 * record's `dateFinished + 1`.
 *
 * Regression context: the original dialog code gated on `record.isActive`, which
 * could be stale `1` for records whose `dateFinished` had already passed.
 * That produced renewals starting in the past for members who had been away
 * for a month.
 */
class RenewalStartDateTest {

    private val today: LocalDate = LocalDate.of(2026, 5, 25)

    private fun record(dateFinished: LocalDate, isActive: Boolean = true): MembershipRecord =
        MembershipRecord(
            id = 0,
            memberId = 1,
            membershipId = 1,
            dateStarted = dateFinished.minusMonths(1),
            dateFinished = dateFinished,
            isActive = isActive,
            isPaid = true
        )

    @Test
    fun `no records returns today`() {
        assertEquals(today, chooseRenewalStartDate(today, emptyList()))
    }

    @Test
    fun `single unexpired record returns its dateFinished + 1`() {
        val active = record(dateFinished = today.plusDays(10))
        assertEquals(today.plusDays(11), chooseRenewalStartDate(today, listOf(active)))
    }

    @Test
    fun `record ending today still counts as unexpired and chains the next day`() {
        // Matches the existing "expires today is still active" convention used by
        // refreshMembershipStatuses (strict `dateFinished < today` for deactivation).
        val endingToday = record(dateFinished = today)
        assertEquals(today.plusDays(1), chooseRenewalStartDate(today, listOf(endingToday)))
    }

    @Test
    fun `all records expired returns today`() {
        // The original bug: this case used to return expired.dateFinished+1 because
        // the stale isActive flag (still 1) was the gate.
        val staleExpired = record(dateFinished = today.minusMonths(1), isActive = true)
        val olderExpired = record(dateFinished = today.minusMonths(3), isActive = false)
        assertEquals(today, chooseRenewalStartDate(today, listOf(staleExpired, olderExpired)))
    }

    @Test
    fun `mix of expired and unexpired chains onto the unexpired one`() {
        val expired = record(dateFinished = today.minusMonths(2))
        val active = record(dateFinished = today.plusDays(5))
        assertEquals(today.plusDays(6), chooseRenewalStartDate(today, listOf(expired, active)))
    }

    @Test
    fun `chains onto the latest pre-bought future record`() {
        // Member already chained: active until +10, pre-bought another to +40.
        // The new record should stack onto +40, not +10.
        val active = record(dateFinished = today.plusDays(10))
        val preBoughtFuture = record(dateFinished = today.plusDays(40))
        assertEquals(today.plusDays(41), chooseRenewalStartDate(today, listOf(active, preBoughtFuture)))
    }
}
