package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.taskaccounting.TaskAccounting
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document

/**
 * <p>
 *     Root entity defining the Project Management data.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class Task @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val projectManagementId: String,
    val projectManagementStageId: String,
    val admins: Set<SimpleContributor> = emptySet(),
    val taskAccounting: TaskAccounting,
    var sourceIntegration: SourceTaskIntegration,
) {
    constructor(
        projectManagementId: String,
        projectManagementStageId: String,
        admins: Set<SimpleContributor> = emptySet(),
        taskAccounting: TaskAccounting,
        sourceIntegration: SourceTaskIntegration,
    ) : this(
        null,
        projectManagementId,
        projectManagementStageId,
        admins,
        taskAccounting,
        sourceIntegration,
    )
}
