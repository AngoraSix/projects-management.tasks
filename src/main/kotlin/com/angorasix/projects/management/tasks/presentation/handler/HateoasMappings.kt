package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.reactive.presentation.mappings.addSelfLink
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectManagementTaskStats
import com.angorasix.projects.management.tasks.presentation.dto.ProjectManagementTaskStatsDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */

fun TaskDto.resolveHypermedia(
    requestingContributor: A6Contributor?,
    task: Task,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): TaskDto {
    val getSingleRoute = apiConfigs.routes.getTask
    // self
    addSelfLink(getSingleRoute, request, listOf(id ?: "unknown"))

    if (task.isAdmin(requestingContributor?.contributorId)) {
        // Here go admin actions
        task.isAdmin(requestingContributor?.contributorId)
    }
    return this
}

fun ProjectManagementTaskStatsDto.resolveHypermedia(
    requestingContributor: A6Contributor?,
    projectManagementTaskStats: ProjectManagementTaskStats,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectManagementTaskStatsDto {
    val getSingleRoute = apiConfigs.routes.getProjectManagementTaskStats
    // self
    addSelfLink(getSingleRoute, request, listOf(projectManagementTaskStats.projectManagementId))

    requestingContributor?.let {
        // Here go admin actions
        println("Requesting contributor: ${it.contributorId}")
    }

    return this
}
