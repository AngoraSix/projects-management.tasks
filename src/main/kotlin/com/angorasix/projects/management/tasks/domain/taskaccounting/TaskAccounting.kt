package com.angorasix.projects.management.tasks.domain.taskaccounting

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * <p>
 *     Root entity defining the Project Management data.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class TaskAccounting @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val earnedCaps: Double,
    val instalmentStartInstant: Instant,
    val instalmentEndInstant: Instant,
    val instalments: List<TaskInstalment>,
    val redemptionFrequency: String = "daily",
) {
    constructor(
        earnedCaps: Double,
        instalmentStartInstant: Instant,
        instalmentEndInstant: Instant,
        instalments: List<TaskInstalment> = emptyList(),
    ) : this(
        null,
        earnedCaps,
        instalmentStartInstant,
        instalmentEndInstant,
        instalments,
    )
}
