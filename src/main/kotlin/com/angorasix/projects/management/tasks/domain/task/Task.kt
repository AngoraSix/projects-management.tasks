package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.commons.domain.SimpleContributor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * <p>
 *     Root entity defining the Project Management Task data.
 * </p>
 *
 * @author rozagerardoO
 */
@Document
data class Task @PersistenceCreator constructor(
    @field:Id val id: String?,
    val projectManagementId: String,
    val title: String,
    val description: String = "",
    val admins: Set<SimpleContributor> = emptySet(),
    var assigneeIds: Set<String> = emptySet(),
    val done: Boolean = false,
    val dueInstant: Instant? = null,
    val estimations: TaskEstimations? = null,
    @Transient val integrationId: String? = null,
) {
    constructor(
        projectManagementId: String,
        title: String,
        description: String = "",
        admins: Set<SimpleContributor> = emptySet(),
        assigneeIds: Set<String> = emptySet(),
        done: Boolean = false,
        dueInstant: Instant? = null,
        estimations: TaskEstimations? = null,
        integrationId: String? = null,
    ) : this(
        null,
        projectManagementId,
        title,
        description,
        admins,
        assigneeIds,
        done,
        dueInstant,
        estimations,
        integrationId,
    )

    /**
     * Checks whether a particular contributor is Admin of this Task.
     *
     * @param contributorId - contributor candidate to check.
     */
    fun isAdmin(contributorId: String?): Boolean =
        (contributorId != null).and(admins.any { it.contributorId == contributorId })
}
