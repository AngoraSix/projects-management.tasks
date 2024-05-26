package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.domain.SourcedContributorWrapper
import com.angorasix.projects.management.tasks.domain.taskaccounting.TaskAccounting
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document

/**
 * <p>
 *     Root entity defining the Project Management Task data.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class Task @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val projectManagementId: String,
    val admins: Set<SimpleContributor> = emptySet(),
    var assignees: Set<SourcedContributorWrapper> = emptySet(),
    var accounting: TaskAccounting?,
    // assignee, status (? or simply if earned caps has value?)
) {
    constructor(
        projectManagementId: String,
        admins: Set<SimpleContributor> = emptySet(),
        assignees: Set<SourcedContributorWrapper> = emptySet(),
        accounting: TaskAccounting?,
    ) : this(
        null,
        projectManagementId,
        admins,
        assignees,
        accounting,
    )
}
