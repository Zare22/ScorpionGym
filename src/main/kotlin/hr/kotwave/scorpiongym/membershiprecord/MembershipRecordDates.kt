package hr.kotwave.scorpiongym.membershiprecord

import java.time.LocalDate

/**
 * Picks the dateStarted for a brand-new MembershipRecord being chained onto
 * a member's existing history.
 *
 * Rule:
 * - If the member has any record whose `dateFinished` is today or in the future,
 *   the new record starts the day after the latest such date (so renewals stack).
 * - Otherwise the new record starts today.
 *
 * Crucially, this looks at `dateFinished` directly rather than the persisted
 * `isActive` flag: an expired record left with `isActive = 1` (because
 * `refreshMembershipStatuses()` hadn't run yet) must NOT cause the renewal to
 * start in the past.
 */
fun chooseRenewalStartDate(today: LocalDate, existingRecords: List<MembershipRecord>): LocalDate {
    val hasUnexpired = existingRecords.any { it.dateFinished >= today }
    if (!hasUnexpired) return today
    val latestEnd = existingRecords.maxOfOrNull { it.dateFinished } ?: return today
    return latestEnd.plusDays(1)
}
