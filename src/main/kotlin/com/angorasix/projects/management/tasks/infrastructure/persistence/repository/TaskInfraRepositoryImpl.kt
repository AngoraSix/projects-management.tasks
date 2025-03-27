package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.domain.ContributorStats
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectManagementTaskStats
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectStats
import com.angorasix.projects.management.tasks.infrastructure.domain.TasksStats
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation.facet
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators
import org.springframework.data.mongodb.core.aggregation.FacetOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class TaskInfraRepositoryImpl(
    private val mongoOps: ReactiveMongoOperations,
) : TaskInfraRepository {
    override fun findUsingFilter(filter: ListTaskFilter): Flow<Task> = mongoOps.find(filter.toQuery(), Task::class.java).asFlow()

    override suspend fun findForContributorUsingFilter(
        filter: ListTaskFilter,
        requestingContributor: SimpleContributor?,
    ): Task? =
        mongoOps
            .find(filter.toQuery(requestingContributor), Task::class.java)
            .awaitFirstOrNull()

    override suspend fun resolveStatsUsingFilter(
        filter: ListTaskFilter,
        requestingContributor: SimpleContributor?,
    ): ProjectManagementTaskStats {
        // Assume filter.projectManagementIds is a non-empty list; take the first value.
        val projectManagementId =
            filter.projectManagementIds?.firstOrNull()
                ?: throw IllegalArgumentException("Project management id is required for stats")

        // Define a threshold for "recently completed" tasks (e.g. tasks completed within the last 7 days)
        val recentThreshold = Instant.now().minus(7, ChronoUnit.DAYS)

        // Match stage: select tasks for the given projectManagementId.
        val matchStage: MatchOperation = match(Criteria.where("projectManagementId").`is`(projectManagementId))

        // --- Facet: Project-level Stats ---
        val projectGroup: GroupOperation =
            group()
                .count()
                .`as`("totalCount")
                .sum(ConditionalOperators.`when`(Criteria.where("done").`is`(true)).then(1).otherwise(0))
                .`as`("completedCount")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria().andOperator(
                                Criteria
                                    .where("done")
                                    .`is`(true),
                                Criteria
                                    .where("doneInstant")
                                    .gte(recentThreshold),
                            ),
                        ).then(1)
                        .otherwise(0),
                ).`as`("recentlyCompletedCount")
                .sum(ConditionalOperators.IfNull.ifNull("\$estimations.effort").then(0.0))
                .`as`("totalEffort")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria.where("done").`is`(true),
                        ).thenValueOf("\$estimations.effort")
                        .otherwise(0.0),
                ).`as`("totalDoneEffort")

        val projectProjection: ProjectionOperation =
            project("totalCount", "completedCount", "recentlyCompletedCount", "totalEffort", "totalDoneEffort")

        // --- Facet: Contributor-level Stats ---
        // Unwind the assigneeIds so each contributor assignment becomes a separate document.
        val unwindAssignees = unwind("assigneeIds")
        val contributorGroup: GroupOperation =
            group("assigneeIds")
                .count()
                .`as`("totalCount")
                .sum(ConditionalOperators.`when`(Criteria.where("done").`is`(true)).then(1).otherwise(0))
                .`as`("completedCount")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria().andOperator(
                                Criteria
                                    .where("done")
                                    .`is`(true),
                                Criteria
                                    .where("doneInstant")
                                    .gte(recentThreshold),
                            ),
                        ).then(1)
                        .otherwise(0),
                ).`as`("recentlyCompletedCount")
                .sum(ConditionalOperators.IfNull.ifNull("\$estimations.effort").then(0.0))
                .`as`("totalEffort")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria.where("done").`is`(true),
                        ).thenValueOf("\$estimations.effort")
                        .otherwise(0.0),
                ).`as`("totalDoneEffort")

        val contributorProjection: ProjectionOperation =
            project()
                .and("_id")
                .`as`("contributorId")
                .andExpression("totalCount")
                .`as`("totalCount")
                .andExpression("completedCount")
                .`as`("completedCount")
                .andExpression("recentlyCompletedCount")
                .`as`("recentlyCompletedCount")
                .and("totalEffort")
                .`as`("totalEffort")
                .and("totalDoneEffort")
                .`as`("totalDoneEffort")
                .andExclude("_id")

        // Build the facet operation:
        val facet: FacetOperation =
            facet(projectGroup, projectProjection)
                .`as`("projectStats")
                .and(unwindAssignees, contributorGroup, contributorProjection)
                .`as`("contributorStats")

        // Overall aggregation pipeline:
        val aggregation =
            newAggregation(
                matchStage,
                facet,
            )

        // Execute the aggregation on the "task" collection.
        val aggResults =
            mongoOps
                .aggregate(aggregation, "task", Document::class.java)
                .awaitFirstOrNull() // assume a single result document from the facet stage

        // If no result is returned, assume zero counts.
        if (aggResults == null) {
            return ProjectManagementTaskStats(
                projectManagementId = projectManagementId,
                project =
                    ProjectStats(
                        tasks = TasksStats(0, 0, 0, 0.0, 0.0),
                        contributors = emptyList(),
                    ),
            )
        }

        // Extract the facet results.
        val projectStatsList: List<Document> = aggResults.getList("projectStats", Document::class.java)
        val contributorStatsList: List<Document> = aggResults.getList("contributorStats", Document::class.java)

        // Build ProjectStats from projectStats facet.
        val projectTasksStats =
            if (projectStatsList.isNotEmpty()) {
                val proj = projectStatsList.first()
                TasksStats(
                    recentlyCompletedCount = proj.getInteger("recentlyCompletedCount", 0),
                    completedCount = proj.getInteger("completedCount", 0),
                    totalCount = proj.getInteger("totalCount", 0),
                    totalEffort = proj.getDouble("totalEffort") ?: 0.0,
                    totalDoneEffort = proj.getDouble("totalDoneEffort") ?: 0.0,
                )
            } else {
                TasksStats(0, 0, 0, 0.0, 0.0)
            }

        // Map contributor facet results.
        val contributorStats =
            contributorStatsList.map { doc ->
                ContributorStats(
                    contributorId = doc.getString("contributorId"),
                    tasks =
                        TasksStats(
                            recentlyCompletedCount = doc.getInteger("recentlyCompletedCount", 0),
                            completedCount = doc.getInteger("completedCount", 0),
                            totalCount = doc.getInteger("totalCount", 0),
                            totalEffort = doc.getDouble("totalEffort") ?: 0.0,
                            totalDoneEffort = doc.getDouble("totalDoneEffort") ?: 0.0,
                        ),
                )
            }

        // Construct the final domain object.
        val projectStats =
            ProjectStats(
                tasks = projectTasksStats,
                contributors = contributorStats,
            )

        return ProjectManagementTaskStats(
            projectManagementId = projectManagementId,
            project = projectStats,
            contributor = requestingContributor?.contributorId?.let { contributorStats.firstOrNull { c -> c.contributorId == it } },
        )
    }
}

private fun ListTaskFilter.toQuery(requestingContributor: SimpleContributor? = null): Query {
    val query = Query()

    ids?.let { query.addCriteria(where("_id").`in`(it as Collection<Any>)) }
    projectManagementIds?.let { query.addCriteria(where("projectId").`in`(it as Collection<Any>)) }

    if (adminId != null && requestingContributor != null) {
        query.addCriteria(where("admins.contributorId").`in`(adminId + requestingContributor.contributorId))
    }

    return query
}
