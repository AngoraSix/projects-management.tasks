package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskEstimations
import com.angorasix.projects.management.tasks.domain.taskaccounting.TaskAccounting
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.infrastructure.domain.ContributorStats
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectManagementTaskStats
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectStats
import com.angorasix.projects.management.tasks.infrastructure.domain.TasksStats
import com.angorasix.projects.management.tasks.presentation.dto.CapsEstimationDto
import com.angorasix.projects.management.tasks.presentation.dto.ContributorStatsDto
import com.angorasix.projects.management.tasks.presentation.dto.ProjectManagementTaskStatsDto
import com.angorasix.projects.management.tasks.presentation.dto.ProjectStatsDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskAccountingDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
import com.angorasix.projects.management.tasks.presentation.dto.TasksStatsDto
import org.springframework.web.reactive.function.server.ServerRequest

fun Task.convertToDto(): TaskDto =
    TaskDto(
        projectManagementId,
        title,
        description,
        admins,
        assigneeIds,
        done,
        doneInstant,
        dueInstant,
        estimations?.convertToDto(),
    )

fun Task.convertToDto(
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): TaskDto = convertToDto().resolveHypermedia(requestingContributor, this, apiConfigs, request)

fun TaskDto.convertToDomain(admins: Set<SimpleContributor>): Task =
    Task(
        projectManagementId,
        title,
        description,
        admins,
        assigneeIds,
        done,
        doneInstant,
        dueInstant,
    )

fun ProjectManagementTaskStats.convertToDto(): ProjectManagementTaskStatsDto =
    ProjectManagementTaskStatsDto(
        projectManagementId,
        project.convertToDto(),
        contributor?.convertToDto(),
    )

fun ProjectManagementTaskStats.convertToDto(
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectManagementTaskStatsDto = convertToDto().resolveHypermedia(requestingContributor, this, apiConfigs, request)

fun ProjectStats.convertToDto(): ProjectStatsDto =
    ProjectStatsDto(
        tasks.convertToDto(),
        contributors.map { it.convertToDto() },
    )

fun TasksStats.convertToDto(): TasksStatsDto =
    TasksStatsDto(
        recentlyCompletedCount,
        completedCount,
        totalCount,
        totalEffort,
        totalDoneEffort,
    )

fun ContributorStats.convertToDto(): ContributorStatsDto =
    ContributorStatsDto(
        contributorId,
        tasks.convertToDto(),
    )

fun TaskAccounting.convertToDto(): TaskAccountingDto =
    TaskAccountingDto(
        taskId,
        earnedCaps,
        redemptionStartInstant,
        redemptionEndInstant,
        redemptionFrequency,
        id,
    )

fun TaskAccountingDto.convertToDomain(): TaskAccounting {
    requireNotNull(taskId)
    requireNotNull(earnedCaps)
    requireNotNull(redemptionStartInstant)
    requireNotNull(redemptionEndInstant)
    return TaskAccounting(
        taskId,
        earnedCaps,
        redemptionStartInstant,
        redemptionEndInstant,
        emptyList(),
    )
}

fun TaskEstimations.convertToDto(): CapsEstimationDto =
    CapsEstimationDto(
        caps = caps,
        strategy = strategy,
        effort = effort,
        complexity = complexity,
        industry = industry,
        industryModifier = industryModifier,
        moneyPayment = moneyPayment,
    )

fun CapsEstimationDto.convertToDomain(): TaskEstimations =
    TaskEstimations(
        caps = caps,
        strategy = strategy,
        effort = effort,
        complexity = complexity,
        industry = industry,
        industryModifier = industryModifier,
        moneyPayment = moneyPayment,
    )
