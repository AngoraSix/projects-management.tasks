package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListProjectsManagementTasksFilter
import kotlinx.coroutines.flow.Flow

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ProjectManagementTasksFilterRepository {
    fun findUsingFilter(filter: ListProjectsManagementTasksFilter): Flow<Task>
    suspend fun findByIdForContributor(
        filter: ListProjectsManagementTasksFilter,
        requestingContributor: SimpleContributor?,
    ): Task?
}
