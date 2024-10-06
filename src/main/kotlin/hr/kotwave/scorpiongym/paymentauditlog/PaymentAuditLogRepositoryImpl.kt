package hr.kotwave.scorpiongym.paymentauditlog

class PaymentAuditLogRepositoryImpl(private val paymentAuditLogDao: PaymentAuditLogDao) : PaymentAuditLogRepository {
    override fun getAllPaymentAuditLogs(): MutableList<PaymentAuditLog> = paymentAuditLogDao.getAllPaymentAuditLogs()
}