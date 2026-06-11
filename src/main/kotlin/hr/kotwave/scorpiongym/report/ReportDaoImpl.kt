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

    companion object {
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
    }
}
