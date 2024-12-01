package com.angorasix.projects.management.tasks.domain.taskaccounting

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * <p>
 *     Root entity defining the Project Management Task Redemptions data.
 *
 *     A Redemption refers to a payment/instalment of the receivable earned by performing the task.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class TaskRedemption @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val percentage: Double,
    val redemptionAmount: Double,
    val redemptionCurrency: String,
    val transactionId: String,
    val date: Instant,
) {
    constructor(
        percentage: Double,
        paymentAmount: Double,
        paymentCurrency: String,
        transactionId: String,
        date: Instant,
    ) : this(
        null,
        percentage,
        paymentAmount,
        paymentCurrency,
        transactionId,
        date,
    )
}
