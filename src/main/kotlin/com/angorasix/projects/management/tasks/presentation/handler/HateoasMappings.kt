package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.core.presentation.dto.ProjectManagementDto
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

fun ProjectManagementDto.resolveHypermedia(
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectManagementDto {
    val getSingleRoute = apiConfigs.routes.getProjectManagement
    // self
    val selfLink =
        Link.of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString())
            .withRel(getSingleRoute.name).expand(id).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    val getByProjectIdRoute = apiConfigs.routes.getProjectManagementByProjectId
    val getByProjectIdLink =
        Link.of(
            uriBuilder(request).path(getByProjectIdRoute.resolvePath())
                .build().toUriString(),
        ).withTitle(getByProjectIdRoute.name)
            .withName(getByProjectIdRoute.name)
            .withRel(getByProjectIdRoute.name).expand(projectId)
    val getByProjectIdAffordanceLink =
        Affordances.of(getByProjectIdLink).afford(HttpMethod.GET)
            .withName(getByProjectIdRoute.name).toLink()
    add(getByProjectIdAffordanceLink)

    // edit ProjectManagement
    if (requestingContributor != null && admins != null) {
        if (admins?.map { it.contributorId }?.contains(requestingContributor.contributorId) == true) {
            val editProjectManagementRoute = apiConfigs.routes.updateProjectManagement
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

fun resolveCreateByProjectIdLink(
    projectId: String,
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): Links {
    val getSingleRoute = apiConfigs.routes.getProjectManagement
    // self (by projectId
    val getByProjectIdRoute = apiConfigs.routes.getProjectManagementByProjectId
    val getByProjectIdLink =
        Link.of(
            uriBuilder(request).path(getByProjectIdRoute.resolvePath())
                .build().toUriString(),
        ).withTitle(getByProjectIdRoute.name)
            .withName(getByProjectIdRoute.name)
            .withRel(getByProjectIdRoute.name).expand(projectId).withSelfRel()
    val getByProjectIdAffordanceLink =
        Affordances.of(getByProjectIdLink).afford(HttpMethod.GET)
            .withName(getByProjectIdRoute.name).toLink()

    // create
    val createRoute = apiConfigs.routes.createProjectManagementByProjectId
    if (requestingContributor != null && requestingContributor.isAdminHint == true) {
        val createLink =
            Link.of(uriBuilder(request).path(createRoute.resolvePath()).build().toUriString())
                .withRel(createRoute.name).expand(projectId)
        val createAffordanceLink =
            Affordances.of(createLink).afford(HttpMethod.POST)
                .withName(createRoute.name).toLink()
        return Links.of(
            getByProjectIdAffordanceLink,
            createAffordanceLink,
        )
    }

    return Links.of(
        getByProjectIdAffordanceLink,
    )
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}
