package com.angorasix.projects.management.tasks.domain.taskaccounting

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * <p>
 *     Root entity defining the Project Management Task Accounting data.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class TaskAccounting @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val taskId: String,
    val earnedCaps: Double,
    val redemptionStartInstant: Instant,
    val redemptionEndInstant: Instant,
    val redemptions: List<TaskRedemption>,
    val redemptionFrequency: String = "daily",
) {
    constructor(
        taskId: String,
        earnedCaps: Double,
        redemptionStartInstant: Instant,
        redemptionEndInstant: Instant,
        redemptions: List<TaskRedemption> = emptyList(),
    ) : this(
        null,
        taskId,
        earnedCaps,
        redemptionStartInstant,
        redemptionEndInstant,
        redemptions,
    )
}
