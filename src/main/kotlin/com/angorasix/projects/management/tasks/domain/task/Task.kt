package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.commons.domain.A6Contributor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
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
data class Task
    @PersistenceCreator
    constructor(
        @field:Id val id: String?,
        val projectManagementId: String,
        val title: String,
        val description: String = "",
        val admins: Set<A6Contributor> = emptySet(),
        var assigneeIds: Set<String> = emptySet(),
        val done: Boolean = false,
        val doneInstant: Instant? = null,
        val dueInstant: Instant? = null,
        val estimations: TaskEstimations? = null,
    ) {
        constructor(
            projectManagementId: String,
            title: String,
            description: String = "",
            admins: Set<A6Contributor> = emptySet(),
            assigneeIds: Set<String> = emptySet(),
            done: Boolean = false,
            doneInstant: Instant? = null,
            dueInstant: Instant? = null,
            estimations: TaskEstimations? = null,
        ) : this(
            id = null,
            projectManagementId = projectManagementId,
            title = title,
            description = description,
            admins = admins,
            assigneeIds = assigneeIds,
            done = done,
            doneInstant = doneInstant,
            dueInstant = dueInstant,
            estimations = estimations,
        )

        /**
         * Checks whether a particular contributor is Admin of this Task.
         *
         * @param contributorId - contributor candidate to check.
         */
        fun isAdmin(contributorId: String?): Boolean = (contributorId != null).and(admins.any { it.contributorId == contributorId })
    }
