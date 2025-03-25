package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskEstimations
import com.angorasix.projects.management.tasks.domain.taskaccounting.TaskAccounting
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.presentation.dto.CapsEstimationDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskAccountingDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
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
