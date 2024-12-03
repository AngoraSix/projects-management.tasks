package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.CapsEstimation
import com.angorasix.projects.management.tasks.domain.task.Task
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
        dueInstant,
        estimation?.convertToDto(),
        integrationId,
    )

fun Task.convertToDto(
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): TaskDto =
    convertToDto().resolveHypermedia(requestingContributor, this, apiConfigs, request)

fun TaskDto.convertToDomain(
    admins: Set<SimpleContributor>,
): Task {
    return Task(
        projectManagementId,
        title,
        description,
        admins,
        assigneeIds,
        done,
        dueInstant,
        estimation?.convertToDomain(),
        integrationId,
    )
}

fun TaskAccounting.convertToDto(): TaskAccountingDto {
    return TaskAccountingDto(
        taskId,
        earnedCaps,
        redemptionStartInstant,
        redemptionEndInstant,
        redemptionFrequency,
        id,
    )
}

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

fun CapsEstimation.convertToDto(): CapsEstimationDto {
    return CapsEstimationDto(estimatedCaps, effort, difficulty, modifier)
}

fun CapsEstimationDto.convertToDomain(): CapsEstimation {
    return CapsEstimation(estimatedCaps, effort, difficulty, modifier)
}
