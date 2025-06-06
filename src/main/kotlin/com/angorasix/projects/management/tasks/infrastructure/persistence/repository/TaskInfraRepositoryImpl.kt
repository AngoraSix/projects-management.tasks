package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

import com.angorasix.commons.domain.A6Contributor
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
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation.facet
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators
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
        requestingContributor: A6Contributor?,
    ): Task? =
        mongoOps
            .find(filter.toQuery(requestingContributor), Task::class.java)
            .awaitFirstOrNull()

    override suspend fun resolveStatsUsingFilter(
        filter: ListTaskFilter,
        requestingContributor: A6Contributor?,
    ): ProjectManagementTaskStats {
        // 1. Get the projectManagementId from filter.
        val projectManagementId = extractProjectManagementId(filter)
        // 2. Define a threshold for "recent" (in days).
        val recentThreshold = Instant.now().minus(filter.recentPeriodDays ?: RECENT_PERIOD_DAYS_DEFAULT, ChronoUnit.DAYS)
        // 3. Build aggregation stages.
        val matchStage = buildMatchStage(projectManagementId)
        val (projectGroup, projectProjection) = buildProjectFacet(recentThreshold)
        val buildContributorFacetOps = buildContributorFacet(recentThreshold, filter.sortField)

        val facetOperation =
            facet(projectGroup, projectProjection)
                .`as`("projectStats")
                .and(
                    buildContributorFacetOps.component1(),
                    buildContributorFacetOps.component2(),
                    buildContributorFacetOps.component3(),
                    buildContributorFacetOps.component4(),
                ).`as`("contributorStats")

        val aggregation = newAggregation(matchStage, facetOperation)

        // 4. Execute the aggregation.
        val aggResult = mongoOps.aggregate(aggregation, "task", Document::class.java).awaitFirstOrNull()

        // 5. If no results, return a zero-stats domain object.
        if (aggResult == null) {
            return ProjectManagementTaskStats(
                projectManagementId = projectManagementId,
                project = ProjectStats(tasks = TasksStats(0, 0, 0, 0.0, 0.0, 0.0), contributors = emptyList()),
            )
        }

        // 6. Extract facet results.
        val projectStatsList = aggResult.getList("projectStats", Document::class.java)
        val contributorStatsList = aggResult.getList("contributorStats", Document::class.java)

        // 7. Map results.
        val projectTasksStats = mapProjectTasksStats(projectStatsList)

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
                            completedEffort = doc.getDouble("completedEffort") ?: 0.0,
                            recentlyCompletedEffort = doc.getDouble("recentlyCompletedEffort") ?: 0.0,
                        ),
                )
            }

        // 8. Build the final domain object.
        return ProjectManagementTaskStats(
            projectManagementId = projectManagementId,
            project = ProjectStats(tasks = projectTasksStats, contributors = contributorStats),
            contributor =
                requestingContributor?.contributorId?.let { id ->
                    contributorStats.firstOrNull { it.contributorId == id }
                },
        )
    }

    // --- Private helper functions ---

    private fun extractProjectManagementId(filter: ListTaskFilter): String =
        filter.projectManagementIds?.firstOrNull()
            ?: throw IllegalArgumentException("Project management id is required for stats")

    private fun buildMatchStage(projectManagementId: String): MatchOperation =
        match(Criteria.where("projectManagementId").`is`(projectManagementId))

    private fun buildProjectFacet(recentThreshold: Instant): Pair<GroupOperation, ProjectionOperation> {
        val groupOp =
            group()
                .count()
                .`as`("totalCount")
                .sum(ConditionalOperators.`when`(Criteria.where("done").`is`(true)).then(1).otherwise(0))
                .`as`("completedCount")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria().andOperator(
                                Criteria.where("done").`is`(true),
                                Criteria.where("doneInstant").gte(recentThreshold),
                            ),
                        ).then(1)
                        .otherwise(0),
                ).`as`("recentlyCompletedCount")
                .sum(ConditionalOperators.IfNull.ifNull("\$estimations.effort").then(0.0))
                .`as`("totalEffort")
                .sum(
                    ConditionalOperators
                        .`when`(Criteria.where("done").`is`(true))
                        .thenValueOf("\$estimations.effort")
                        .otherwise(0.0),
                ).`as`("completedEffort")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria().andOperator(
                                Criteria.where("done").`is`(true),
                                Criteria.where("doneInstant").gte(recentThreshold),
                            ),
                        ).thenValueOf("\$estimations.effort")
                        .otherwise(0.0),
                ).`as`("recentlyCompletedEffort")

        val projectionOp =
            project("totalCount", "completedCount", "recentlyCompletedCount", "totalEffort", "completedEffort", "recentlyCompletedEffort")
        return Pair(groupOp, projectionOp)
    }

    private fun buildContributorFacet(
        recentThreshold: Instant,
        sortField: String?,
    ): List<AggregationOperation> {
        val unwindOp = unwind("assigneeIds")
        val groupOp =
            group("assigneeIds")
                .count()
                .`as`("totalCount")
                .sum(ConditionalOperators.`when`(Criteria.where("done").`is`(true)).then(1).otherwise(0))
                .`as`("completedCount")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria().andOperator(
                                Criteria.where("done").`is`(true),
                                Criteria.where("doneInstant").gte(recentThreshold),
                            ),
                        ).then(1)
                        .otherwise(0),
                ).`as`("recentlyCompletedCount")
                .sum(ConditionalOperators.IfNull.ifNull("\$estimations.effort").then(0.0))
                .`as`("totalEffort")
                .sum(
                    ConditionalOperators
                        .`when`(Criteria.where("done").`is`(true))
                        .thenValueOf("\$estimations.effort")
                        .otherwise(0.0),
                ).`as`("completedEffort")
                .sum(
                    ConditionalOperators
                        .`when`(
                            Criteria().andOperator(
                                Criteria.where("done").`is`(true),
                                Criteria.where("doneInstant").gte(recentThreshold),
                            ),
                        ).thenValueOf("\$estimations.effort")
                        .otherwise(0.0),
                ).`as`("recentlyCompletedEffort")

        val projectionOp =
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
                .and("completedEffort")
                .`as`("completedEffort")
                .and("recentlyCompletedEffort")
                .`as`("recentlyCompletedEffort")
                .andExclude("_id")

        val sort =
            sort(Sort.by(Sort.Direction.DESC, if (sortField.isNullOrBlank()) "totalCount" else sortField))

        return listOf(unwindOp, groupOp, projectionOp, sort)
    }

    private fun mapProjectTasksStats(projectStatsList: List<Document>): TasksStats =
        if (projectStatsList.isNotEmpty()) {
            val proj = projectStatsList.first()
            TasksStats(
                recentlyCompletedCount = proj.getInteger("recentlyCompletedCount", 0),
                completedCount = proj.getInteger("completedCount", 0),
                totalCount = proj.getInteger("totalCount", 0),
                totalEffort = proj.getDouble("totalEffort") ?: 0.0,
                completedEffort = proj.getDouble("completedEffort") ?: 0.0,
                recentlyCompletedEffort = proj.getDouble("recentlyCompletedEffort") ?: 0.0,
            )
        } else {
            TasksStats(0, 0, 0, 0.0, 0.0, 0.0)
        }

    companion object {
        const val RECENT_PERIOD_DAYS_DEFAULT = 31L
    }
}

private fun ListTaskFilter.toQuery(requestingContributor: A6Contributor? = null): Query {
    val query = Query()

    ids?.let { query.addCriteria(where("_id").`in`(it as Collection<Any>)) }
    projectManagementIds?.let { query.addCriteria(where("projectId").`in`(it as Collection<Any>)) }

    if (adminId != null && requestingContributor != null) {
        query.addCriteria(where("admins.contributorId").`in`(adminId + requestingContributor.contributorId))
    }

    return query
}
