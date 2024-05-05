package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListProjectsManagementTasksFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectManagementTasksFilterRepositoryImpl(val mongoOps: ReactiveMongoOperations) :
    ProjectManagementTasksFilterRepository {

    override fun findUsingFilter(filter: ListProjectsManagementTasksFilter): Flow<Task> {
        return mongoOps.find(filter.toQuery(), Task::class.java).asFlow()
    }

    override suspend fun findByIdForContributor(
        filter: ListProjectsManagementTasksFilter,
        requestingContributor: SimpleContributor?,
    ): Task? {
        return mongoOps.find(filter.toQuery(requestingContributor), Task::class.java)
            .awaitFirstOrNull()
    }
}

private fun ListProjectsManagementTasksFilter.toQuery(requestingContributor: SimpleContributor? = null): Query {
    val query = Query()

    ids?.let { query.addCriteria(where("_id").`in`(it)) }
    projectIds?.let { query.addCriteria(where("projectId").`in`(it)) }

    if (adminId != null && requestingContributor != null) {
        query.addCriteria(where("admins.contributorId").`in`(adminId + requestingContributor.contributorId))
    }

    return query
}
