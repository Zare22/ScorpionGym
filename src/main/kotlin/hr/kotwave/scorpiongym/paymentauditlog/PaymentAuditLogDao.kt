package hr.kotwave.scorpiongym.paymentauditlog

interface PaymentAuditLogDao {
    fun getAllPaymentAuditLogs(): MutableList<PaymentAuditLog>
}