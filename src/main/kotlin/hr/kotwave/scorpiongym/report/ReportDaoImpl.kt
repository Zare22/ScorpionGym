package hr.kotwave.scorpiongym.report

import java.sql.Connection
import java.time.LocalDate

class ReportDaoImpl(private val dbConnection: Connection) : ReportDao {

    override fun membershipSalesByType(from: LocalDate?, to: LocalDate?): MembershipSalesReport {
        // LocalDate.toString() is ISO yyyy-MM-dd, matching the text dates stored in
        // dateStarted / changedAt. A null bound becomes an open end via COALESCE.
        val fromStr = from?.toString()
        val toStr = to?.toString()

        val rows = mutableListOf<MembershipSalesRow>()
        dbConnection.prepareStatement(MEMBERSHIP_SALES_QUERY).use { statement ->
            statement.setString(1, fromStr) // dateStarted from
            statement.setString(2, toStr)   // dateStarted to
            statement.setString(3, fromStr) // changedAt from
            statement.setString(4, toStr)   // changedAt to
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                rows.add(
                    MembershipSalesRow(
                        membershipId = resultSet.getInt("membershipId"),
                        membershipName = resultSet.getString("membershipName"),
                        soldCount = resultSet.getInt("soldCount"),
                        netCollected = resultSet.getDouble("netCollected"),
                    )
                )
            }
        }

        var walkInCount = 0
        dbConnection.prepareStatement(WALK_IN_COUNT_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) walkInCount = resultSet.getInt(1)
        }

        var walkInCollected = 0.0
        dbConnection.prepareStatement(WALK_IN_COLLECTED_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) walkInCollected = resultSet.getDouble(1)
        }

        return MembershipSalesReport(
            rows = rows,
            walkInCount = walkInCount,
            walkInCollected = walkInCollected,
        )
    }

    override fun revenueBreakdown(from: LocalDate?, to: LocalDate?): RevenueBreakdownReport {
        val fromStr = from?.toString()
        val toStr = to?.toString()

        val byCategory = mutableMapOf<RevenueCategory, Double>()
        dbConnection.prepareStatement(REVENUE_BREAKDOWN_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val code = resultSet.getString("category")
                val category = RevenueCategory.entries.firstOrNull { it.name == code } ?: continue
                byCategory[category] = resultSet.getDouble("netCollected")
            }
        }

        // Always emit all four categories in declaration order (zeros included).
        val rows = RevenueCategory.entries.map { RevenueCategoryRow(it, byCategory[it] ?: 0.0) }
        return RevenueBreakdownReport(rows)
    }

    override fun revenueOverTime(from: LocalDate?, to: LocalDate?): RevenueOverTimeReport {
        val fromStr = from?.toString()
        val toStr = to?.toString()

        val rows = mutableListOf<MonthlyRevenueRow>()
        dbConnection.prepareStatement(REVENUE_OVER_TIME_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                rows.add(
                    MonthlyRevenueRow(
                        month = resultSet.getString("ym"),
                        netCollected = resultSet.getDouble("netCollected"),
                    )
                )
            }
        }
        return RevenueOverTimeReport(rows)
    }

    override fun newMembersByMonth(from: LocalDate?, to: LocalDate?): NewMembersReport {
        val fromStr = from?.toString()
        val toStr = to?.toString()

        val rows = mutableListOf<NewMembersRow>()
        dbConnection.prepareStatement(NEW_MEMBERS_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                rows.add(
                    NewMembersRow(
                        month = resultSet.getString("ym"),
                        count = resultSet.getInt("newMembers"),
                    )
                )
            }
        }
        return NewMembersReport(rows)
    }

    override fun outstanding(from: LocalDate?, to: LocalDate?): OutstandingReport {
        val fromStr = from?.toString()
        val toStr = to?.toString()

        val rows = mutableListOf<UnpaidItemRow>()
        dbConnection.prepareStatement(OUTSTANDING_QUERY).use { statement ->
            // Three (from, to) pairs — one per UNION branch.
            statement.setString(1, fromStr); statement.setString(2, toStr)
            statement.setString(3, fromStr); statement.setString(4, toStr)
            statement.setString(5, fromStr); statement.setString(6, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                rows.add(
                    UnpaidItemRow(
                        memberName = resultSet.getString("memberName"),
                        description = resultSet.getString("description"),
                        amount = resultSet.getDouble("amount"),
                        date = LocalDate.parse(resultSet.getString("itemDate")),
                    )
                )
            }
        }
        return OutstandingReport(rows)
    }

    override fun utilization(from: LocalDate?, to: LocalDate?): UtilizationReport {
        val fromStr = from?.toString()
        val toStr = to?.toString()

        // Weekday: strftime('%w') is 0=Sunday..6=Saturday; remap to ISO 1=Mon..7=Sun.
        val byDow = mutableMapOf<Int, Int>()
        dbConnection.prepareStatement(UTILIZATION_WEEKDAY_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val w = resultSet.getString("dow").toInt()
                val isoDay = if (w == 0) 7 else w
                byDow[isoDay] = resultSet.getInt("cnt")
            }
        }
        val byWeekday = (1..7).map { WeekdayCount(it, byDow[it] ?: 0) }

        val byHour = mutableListOf<HourCount>()
        dbConnection.prepareStatement(UTILIZATION_HOUR_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                byHour.add(HourCount(resultSet.getString("hour").toInt(), resultSet.getInt("cnt")))
            }
        }

        val byMonth = mutableListOf<MonthCount>()
        dbConnection.prepareStatement(UTILIZATION_MONTH_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                byMonth.add(MonthCount(resultSet.getString("ym"), resultSet.getInt("cnt")))
            }
        }

        return UtilizationReport(byWeekday = byWeekday, byHour = byHour, byMonth = byMonth)
    }

    override fun demographics(from: LocalDate?, to: LocalDate?): DemographicsReport {
        val fromStr = from?.toString()
        val toStr = to?.toString()

        val byGender = mutableListOf<DemographicRow>()
        dbConnection.prepareStatement(GENDER_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                byGender.add(DemographicRow(resultSet.getString("label"), resultSet.getInt("cnt")))
            }
        }

        val bandCounts = mutableMapOf<String, Int>()
        dbConnection.prepareStatement(AGE_BAND_QUERY).use { statement ->
            statement.setString(1, fromStr)
            statement.setString(2, toStr)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                bandCounts[resultSet.getString("band")] = resultSet.getInt("cnt")
            }
        }
        // Fixed band order, zeros included.
        val byAgeBand = AGE_BANDS.map { DemographicRow(it, bandCounts[it] ?: 0) }

        return DemographicsReport(byGender = byGender, byAgeBand = byAgeBand)
    }

    companion object {

        private val AGE_BANDS =
            listOf("do 18", "18-25", "26-35", "36-45", "46-55", "56-65", "65+", "Nepoznato")
        // One row per membership type. "soldCount" counts member memberships started in
        // the period; "netCollected" sums ledger payments (net of reversals) booked in the
        // period for member memberships. Joining the payment side through MembershipRecord
        // naturally excludes walk-in payments (those carry unregisteredServiceId instead).
        private val MEMBERSHIP_SALES_QUERY = """
            SELECT
                m.id   AS membershipId,
                m.name AS membershipName,
                COALESCE(sold.cnt, 0)            AS soldCount,
                COALESCE(paid.netCollected, 0.0) AS netCollected
            FROM Membership m
            LEFT JOIN (
                SELECT mr.membershipId AS mid, COUNT(*) AS cnt
                FROM MembershipRecord mr
                WHERE mr.dateStarted BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
                GROUP BY mr.membershipId
            ) sold ON sold.mid = m.id
            LEFT JOIN (
                SELECT mr.membershipId AS mid,
                       SUM((pal.isPaidNew - pal.isPaidOld) * pal.price) AS netCollected
                FROM PaymentAuditLog pal
                JOIN MembershipRecord mr ON pal.membershipRecordId = mr.id
                WHERE pal.changedAt BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
                GROUP BY mr.membershipId
            ) paid ON paid.mid = m.id
            ORDER BY m.name
        """.trimIndent()

        // Walk-in membership purchases counted by dateOfService (a DATETIME, hence date()).
        private val WALK_IN_COUNT_QUERY = """
            SELECT COUNT(*)
            FROM UnregisteredService
            WHERE membershipId IS NOT NULL
              AND date(dateOfService) BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
        """.trimIndent()

        // Net cash booked in the period from walk-in membership purchases.
        private val WALK_IN_COLLECTED_QUERY = """
            SELECT COALESCE(SUM((pal.isPaidNew - pal.isPaidOld) * pal.price), 0.0)
            FROM PaymentAuditLog pal
            JOIN UnregisteredService u ON pal.unregisteredServiceId = u.id
            WHERE u.membershipId IS NOT NULL
              AND pal.changedAt BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
        """.trimIndent()

        // Net cash per category. Every ledger row has exactly one source set; the
        // walk-in source is sub-split by whether it bought a membership or a service.
        // Category codes match RevenueCategory enum names.
        private val REVENUE_BREAKDOWN_QUERY = """
            SELECT
                CASE
                    WHEN pal.membershipRecordId   IS NOT NULL THEN 'MEMBER_MEMBERSHIP'
                    WHEN pal.memberOtherServiceId IS NOT NULL THEN 'MEMBER_SERVICE'
                    WHEN u.membershipId           IS NOT NULL THEN 'WALKIN_MEMBERSHIP'
                    WHEN u.otherServiceId         IS NOT NULL THEN 'WALKIN_SERVICE'
                    ELSE 'OTHER'
                END AS category,
                SUM((pal.isPaidNew - pal.isPaidOld) * pal.price) AS netCollected
            FROM PaymentAuditLog pal
            LEFT JOIN UnregisteredService u ON pal.unregisteredServiceId = u.id
            WHERE pal.changedAt BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY category
        """.trimIndent()

        // Net cash per calendar month (YYYY-MM taken from the changedAt text date), oldest first.
        private val REVENUE_OVER_TIME_QUERY = """
            SELECT substr(pal.changedAt, 1, 7) AS ym,
                   SUM((pal.isPaidNew - pal.isPaidOld) * pal.price) AS netCollected
            FROM PaymentAuditLog pal
            WHERE pal.changedAt BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY ym
            ORDER BY ym
        """.trimIndent()

        // New members per calendar month from Member.signedUpDate (YYYY-MM), oldest first.
        private val NEW_MEMBERS_QUERY = """
            SELECT substr(signedUpDate, 1, 7) AS ym, COUNT(*) AS newMembers
            FROM Member
            WHERE signedUpDate BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY ym
            ORDER BY ym
        """.trimIndent()

        // Every unpaid item across the three sources, valued at current list price,
        // newest first. Member items carry a name; walk-in items have NULL memberName.
        private val OUTSTANDING_QUERY = """
            SELECT (m.name || ' ' || m.surname) AS memberName,
                   ms.name AS description, ms.price AS amount, mr.dateStarted AS itemDate
            FROM MembershipRecord mr
            JOIN Membership ms ON ms.id = mr.membershipId
            JOIN Member m ON m.id = mr.memberId
            WHERE mr.isPaid = 0
              AND mr.dateStarted BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            UNION ALL
            SELECT (m.name || ' ' || m.surname) AS memberName,
                   os.name AS description, os.price AS amount, date(mos.dateOfService) AS itemDate
            FROM MemberOtherService mos
            JOIN OtherService os ON os.id = mos.otherServiceId
            JOIN Member m ON m.id = mos.memberId
            WHERE mos.isPaid = 0
              AND date(mos.dateOfService) BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            UNION ALL
            SELECT NULL AS memberName,
                   COALESCE(os.name, ms.name) AS description,
                   COALESCE(os.price, ms.price) AS amount, date(u.dateOfService) AS itemDate
            FROM UnregisteredService u
            LEFT JOIN OtherService os ON os.id = u.otherServiceId
            LEFT JOIN Membership ms ON ms.id = u.membershipId
            WHERE u.isPaid = 0
              AND date(u.dateOfService) BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            ORDER BY itemDate DESC
        """.trimIndent()

        // SQLite's strftime/date accept the ISO 'T' separator stored by LocalDateTime.toString().
        private val UTILIZATION_WEEKDAY_QUERY = """
            SELECT strftime('%w', sessionDateTime) AS dow, COUNT(*) AS cnt
            FROM TrainingSession
            WHERE date(sessionDateTime) BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY dow
        """.trimIndent()

        private val UTILIZATION_HOUR_QUERY = """
            SELECT strftime('%H', sessionDateTime) AS hour, COUNT(*) AS cnt
            FROM TrainingSession
            WHERE date(sessionDateTime) BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY hour
            ORDER BY hour
        """.trimIndent()

        private val UTILIZATION_MONTH_QUERY = """
            SELECT substr(sessionDateTime, 1, 7) AS ym, COUNT(*) AS cnt
            FROM TrainingSession
            WHERE date(sessionDateTime) BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY ym
            ORDER BY ym
        """.trimIndent()

        // Stored gender values are MALE/FEMALE; blanks/nulls become "Nepoznato".
        private val GENDER_QUERY = """
            SELECT CASE
                WHEN gender = 'MALE' THEN 'Muško'
                WHEN gender = 'FEMALE' THEN 'Žensko'
                WHEN gender IS NULL OR TRIM(gender) = '' THEN 'Nepoznato'
                ELSE gender
            END AS label, COUNT(*) AS cnt
            FROM Member
            WHERE signedUpDate BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
            GROUP BY label
            ORDER BY cnt DESC
        """.trimIndent()

        // Age computed as of today; band labels must match AGE_BANDS exactly.
        private val AGE_BAND_QUERY = """
            SELECT band, COUNT(*) AS cnt FROM (
                SELECT CASE
                    WHEN dateOfBirth IS NULL OR TRIM(dateOfBirth) = '' THEN 'Nepoznato'
                    WHEN age < 18 THEN 'do 18'
                    WHEN age BETWEEN 18 AND 25 THEN '18-25'
                    WHEN age BETWEEN 26 AND 35 THEN '26-35'
                    WHEN age BETWEEN 36 AND 45 THEN '36-45'
                    WHEN age BETWEEN 46 AND 55 THEN '46-55'
                    WHEN age BETWEEN 56 AND 65 THEN '56-65'
                    ELSE '65+'
                END AS band
                FROM (
                    SELECT dateOfBirth,
                        (CAST(strftime('%Y', 'now') AS INTEGER) - CAST(strftime('%Y', dateOfBirth) AS INTEGER)
                         - (CASE WHEN strftime('%m-%d', 'now') < strftime('%m-%d', dateOfBirth) THEN 1 ELSE 0 END)) AS age
                    FROM Member
                    WHERE signedUpDate BETWEEN COALESCE(?, '0001-01-01') AND COALESCE(?, '9999-12-31')
                )
            )
            GROUP BY band
        """.trimIndent()
    }
}
