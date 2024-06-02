package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import com.mongodb.bulk.BulkWriteResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class DefaultTaskInfraRepository(val mongoOps: ReactiveMongoOperations) :
    TaskInfraRepository {

    override fun findUsingFilter(filter: ListTaskFilter): Flow<Task> {
        return mongoOps.find(filter.toQuery(), Task::class.java).asFlow()
    }

    override suspend fun findByIdForContributor(
        filter: ListTaskFilter,
        requestingContributor: SimpleContributor?,
    ): Task? {
        return mongoOps.find(filter.toQuery(requestingContributor), Task::class.java)
            .awaitFirstOrNull()
    }

    override suspend fun updateOrCreate(tasks: List<Task>): BulkResult {
        val bulkOps = mongoOps.bulkOps(
            BulkOperations.BulkMode.UNORDERED, Task::class.java,
        )
        tasks.forEach {
            if (it.id != null) {
                bulkOps.updateOne(Query(where("id").`is`(it.id)), updateDefinition(it)) //, Task::class.java)
            } else {
                bulkOps.insert(it)
            }
        }
        return bulkOps.execute().awaitFirst().toDto()
    }
}

private fun updateDefinition(task: Task): UpdateDefinition =
    Update().set("title", task.title).set("description", task.description).set("estimation", task.estimation)
        .set("assignees", task.assignees)

private fun ListTaskFilter.toQuery(requestingContributor: SimpleContributor? = null): Query {
    val query = Query()

    ids?.let { query.addCriteria(where("_id").`in`(it)) }
    projectManagementIds?.let { query.addCriteria(where("projectId").`in`(it)) }

    if (adminId != null && requestingContributor != null) {
        query.addCriteria(where("admins.contributorId").`in`(adminId + requestingContributor.contributorId))
    }

    return query
}

private fun BulkWriteResult.toDto() = BulkResult(insertedCount, modifiedCount)