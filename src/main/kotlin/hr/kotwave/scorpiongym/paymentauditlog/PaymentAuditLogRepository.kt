package hr.kotwave.scorpiongym.paymentauditlog

interface PaymentAuditLogRepository {
    fun getAllPaymentAuditLogs(): MutableList<PaymentAuditLog>
}