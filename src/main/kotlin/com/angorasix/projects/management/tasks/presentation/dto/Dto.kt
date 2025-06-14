package com.angorasix.projects.management.tasks.presentation.dto

import com.angorasix.commons.domain.A6Contributor
import org.springframework.hateoas.RepresentationModel
import java.time.Instant

/**
 *
 *
 * @author rozagerardo
 */
data class TaskDto(
    val projectManagementId: String,
    val title: String,
    val description: String = "",
    val admins: Set<A6Contributor> = emptySet(),
    val assigneeIds: Set<String> = emptySet(),
    val done: Boolean = false,
    val doneInstant: Instant? = null,
    val dueInstant: Instant? = null,
    val estimation: CapsEstimationDto? = null,
    val id: String? = null,
) : RepresentationModel<TaskDto>()

data class CapsEstimationDto(
    val caps: Double? = null,
    val strategy: String? = null,
    val effort: Double? = null,
    val complexity: Double? = null,
    val industry: String? = null,
    val industryModifier: Double? = null,
    val moneyPayment: Double? = null,
)

data class ProjectManagementTaskStatsDto(
    val projectManagementId: String,
    val project: ProjectStatsDto,
    val contributor: ContributorStatsDto? = null,
) : RepresentationModel<ProjectManagementTaskStatsDto>()

data class ProjectStatsDto(
    val tasks: TasksStatsDto,
    val contributors: List<ContributorStatsDto> = emptyList(),
)

data class TasksStatsDto(
    val recentlyCompletedCount: Int,
    val completedCount: Int,
    val totalCount: Int,
    val totalEffort: Double,
    val completedEffort: Double,
    val recentlyCompletedEffort: Double,
)

data class ContributorStatsDto(
    val contributorId: String,
    val tasks: TasksStatsDto,
)
