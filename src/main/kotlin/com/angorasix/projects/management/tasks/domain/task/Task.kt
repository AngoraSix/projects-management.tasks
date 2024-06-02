package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.integrations.SourcedContributorWrapper
import com.angorasix.projects.management.tasks.domain.taskaccounting.TaskAccounting
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

/**
 * <p>
 *     Root entity defining the Project Management Task data.
 * </p>
 *
 * @author rozagerardoO
 */
@Document
data class Task @PersistenceCreator public constructor(
    @field:Id val id: String?,
    val projectManagementId: String,
    val admins: Set<SimpleContributor> = emptySet(),
    var assignees: Set<SourcedContributorWrapper> = emptySet(),
    val title: String,
    val description: String = "",
    val estimation: CapsEstimation?,
    @Transient val sourceTaskId: String? = null
    // assignee, status (? or simply if earned caps has value?)
) {
    constructor(
        projectManagementId: String,
        admins: Set<SimpleContributor> = emptySet(),
        assignees: Set<SourcedContributorWrapper> = emptySet(),
        title: String,
        description: String = "",
        estimation: CapsEstimation?,
        sourceId: String?
    ) : this(
        null,
        projectManagementId,
        admins,
        assignees,
        title,
        description,
        estimation,
        sourceId
    )
}
