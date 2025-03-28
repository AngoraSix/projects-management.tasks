package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectManagementTaskStats
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import kotlinx.coroutines.flow.Flow

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface TaskInfraRepository {
    fun findUsingFilter(filter: ListTaskFilter): Flow<Task>

    suspend fun findForContributorUsingFilter(
        filter: ListTaskFilter,
        requestingContributor: SimpleContributor?,
    ): Task?

    suspend fun resolveStatsUsingFilter(
        filter: ListTaskFilter,
        requestingContributor: SimpleContributor?,
    ): ProjectManagementTaskStats
}
