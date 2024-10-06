package hr.kotwave.scorpiongym.paymentauditlog

import hr.kotwave.scorpiongym.appuser.AppUser
import hr.kotwave.scorpiongym.memberotherservice.MemberOtherService
import hr.kotwave.scorpiongym.membershiprecord.MembershipRecord
import hr.kotwave.scorpiongym.unregisteredservice.UnregisteredService
import hr.kotwave.scorpiongym.util.parseToLocalDateTime
import java.sql.Connection
import java.time.LocalDate

class PaymentAuditLogDaoImpl(private val dbConnection: Connection) : PaymentAuditLogDao {
    override fun getAllPaymentAuditLogs(): MutableList<PaymentAuditLog> {
        val paymentAuditLogs = mutableListOf<PaymentAuditLog>()
        val query = "SELECT * FROM PaymentAuditLogView ORDER BY changedAt"


        dbConnection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                var membershipRecord: MembershipRecord? = null
                var memberOtherService: MemberOtherService? = null
                var unregisteredService: UnregisteredService? = null

                val membershipRecordId = resultSet.getInt("membershipRecordId")
                val memberOtherServiceId = resultSet.getInt("memberOtherServiceId")
                val unregisteredServiceId = resultSet.getInt("unregisteredServiceId")

                if (membershipRecordId != 0)
                    membershipRecord = MembershipRecord(
                        id = membershipRecordId,
                        memberId = resultSet.getInt("membershipMemberId"),
                        membershipId = resultSet.getInt("membershipId"),
                        dateStarted = LocalDate.parse(resultSet.getString("membershipDateStarted")),
                        dateFinished = LocalDate.parse(resultSet.getString("membershipDateFinished")),
                        isActive = resultSet.getBoolean("membershipIsActive"),
                        isPaid = resultSet.getBoolean("membershipIsPaid")
                    )

                if (memberOtherServiceId != 0)
                    memberOtherService = MemberOtherService(
                        id = resultSet.getInt("memberOtherServiceId"),
                        dateOfService = parseToLocalDateTime(resultSet.getString("memberOtherServiceDateOfService")),
                        isPaid = resultSet.getBoolean("memberOtherServiceIsPaid"),
                        memberId = resultSet.getInt("memberOtherServiceMemberId"),
                        otherServiceId = resultSet.getInt("memberOtherServiceOtherServiceId")
                    )

                if (unregisteredServiceId != 0)
                    unregisteredService = UnregisteredService(
                        id = resultSet.getInt("unregisteredServiceId"),
                        dateOfService = parseToLocalDateTime(resultSet.getString("unregisteredServiceDateOfService")),
                        isPaid = resultSet.getBoolean("unregisteredServiceIsPaid"),
                        membershipId = resultSet.getInt("unregisteredServiceMembershipId").takeIf { it != 0 },
                        otherServiceId = resultSet.getInt("unregisteredServiceOtherServiceId").takeIf { it != 0 }
                    )

                val appUser = AppUser(
                    id = resultSet.getInt("appUserId"),
                    username = resultSet.getString("appUsername")
                )

                val paymentAuditLog = PaymentAuditLog(
                    id = resultSet.getInt("paymentAuditLogId"),
                    membershipRecord = membershipRecord,
                    memberOtherService = memberOtherService,
                    unregisteredService = unregisteredService,
                    isPaidOld = resultSet.getBoolean("isPaidOld"),
                    isPaidNew = resultSet.getBoolean("isPaidNew"),
                    price = resultSet.getDouble("price"),
                    changedAt = LocalDate.parse(resultSet.getString("changedAt")),
                    appUser = appUser,
                    isUnregisteredServiceMembership = resultSet.getInt("isUnregisteredServiceMembership") == 1
                )

                paymentAuditLogs.add(paymentAuditLog)
            }
        }
        return paymentAuditLogs
    }
}