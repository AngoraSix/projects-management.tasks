package com.angorasix.projects.management.tasks.domain.taskaccounting

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.Currency

/**
 * <p>
 *     Root entity defining the Project Management data.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class TaskInstalment @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val percentage: Double,
    val paymentAmount: Double,
    val paymentCurrency: String,
    val transactionId: String,
    val date: Instant
) {
    constructor(
        percentage: Double,
        paymentAmount: Double,
        paymentCurrency: String,
        transactionId: String,
        date: Instant
    ) : this(
        null,
        percentage,
        paymentAmount,
        paymentCurrency,
        transactionId,
        date
    )
}
