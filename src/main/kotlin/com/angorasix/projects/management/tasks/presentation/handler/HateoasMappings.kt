package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectManagementTaskStats
import com.angorasix.projects.management.tasks.presentation.dto.ProjectManagementTaskStatsDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.util.ForwardedHeaderUtils

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */

fun TaskDto.resolveHypermedia(
    requestingContributor: SimpleContributor?,
    task: Task,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): TaskDto {
    val getSingleRoute = apiConfigs.routes.getTask
    // self
    val selfLink =
        Link
            .of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString())
            .withRel(getSingleRoute.name)
            .expand(id)
            .withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances
            .of(selfLink)
            .afford(HttpMethod.OPTIONS)
            .withName("default")
            .toLink()
    add(selfLinkWithDefaultAffordance)

    if (task.isAdmin(requestingContributor?.contributorId)) {
        // Here go admin actions
        task.isAdmin(requestingContributor?.contributorId)
    }
    return this
}

fun ProjectManagementTaskStatsDto.resolveHypermedia(
    requestingContributor: SimpleContributor?,
    projectManagementTaskStats: ProjectManagementTaskStats,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectManagementTaskStatsDto {
    val getSingleRoute = apiConfigs.routes.getProjectManagementTaskStats
    // self
    val selfLink =
        Link
            .of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString())
            .withRel(getSingleRoute.name)
            .expand(projectManagementTaskStats.projectManagementId)
            .withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances
            .of(selfLink)
            .afford(HttpMethod.OPTIONS)
            .withName("default")
            .toLink()
    add(selfLinkWithDefaultAffordance)

    requestingContributor?.let {
        // Here go admin actions
        add(selfLinkWithDefaultAffordance)
    }

    return this
}

private fun uriBuilder(request: ServerRequest) =
    request.requestPath().contextPath().let {
        ForwardedHeaderUtils
            .adaptFromForwardedHeaders(request.exchange().request.uri, request.exchange().request.headers)
            .replacePath(it.toString())
            .replaceQuery("")
    }
