package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.util.UriComponentsBuilder

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */

fun TaskDto.resolveHypermedia(
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): TaskDto {
    val getSingleRoute = apiConfigs.routes.getTask
    // self
    val selfLink =
        Link.of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString())
            .withRel(getSingleRoute.name).expand(id).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    // edit Task
    if (requestingContributor != null && admins != null) {
        if (admins.map { it.contributorId }.contains(requestingContributor.contributorId)) {
            val editProjectManagementRoute = apiConfigs.routes.updateTask
            val editProjectManagementLink =
                Link.of(
                    uriBuilder(request).path(editProjectManagementRoute.resolvePath())
                        .build().toUriString(),
                ).withTitle(editProjectManagementRoute.name)
                    .withName(editProjectManagementRoute.name)
                    .withRel(editProjectManagementRoute.name).expand(id)
            val editProjectManagementAffordanceLink =
                Affordances.of(editProjectManagementLink).afford(HttpMethod.PUT)
                    .withName(editProjectManagementRoute.name).toLink()
            add(editProjectManagementAffordanceLink)
        }
    }
    return this
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}
