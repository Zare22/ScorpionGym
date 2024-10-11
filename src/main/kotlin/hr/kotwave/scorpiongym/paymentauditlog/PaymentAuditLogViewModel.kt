package hr.kotwave.scorpiongym.paymentauditlog

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate

class PaymentAuditLogViewModel(private val paymentAuditLogDao: PaymentAuditLogDao) {
    private val _paymentAuditLogs = mutableStateListOf<PaymentAuditLog>()

    fun initPaymentAuditLogs() {
        _paymentAuditLogs.clear()
        val loadedPaymentAuditLogs = paymentAuditLogDao.getAllPaymentAuditLogs()
        _paymentAuditLogs.addAll(loadedPaymentAuditLogs)
    }

    fun getUserTotalByDateRange(from: LocalDate?, to: LocalDate?): Map<String, Double> {
        val filteredLogs = _paymentAuditLogs.filter {
            val logDate = it.changedAt
            when {
                from != null && to != null -> logDate!! in from..to
                from != null -> logDate!! >= from
                to != null -> logDate!! <= to
                else -> true
            }
        }

        return filteredLogs.groupBy { it.appUser!!.username }
            .mapValues { (_, logs) ->
                logs.sumOf { log ->
                    var total = 0.0
                    if (log.isPaidOld!!) total -= log.price!!
                    if (log.isPaidNew!!) total += log.price!!
                    total
                }
            }
    }
}