package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import com.angorasix.projects.management.tasks.presentation.dto.ProjectsManagementTasksQueryParams
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.MediaTypes
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * ProjectManagementTask Handler (Controller) containing all handler functions
 * related to ProjectManagementTask endpoints.
 *
 * @author rozagerardo
 */
class ProjectManagementTasksHandler(
    private val service: ProjectsManagementTasksService,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Handler for the Get All Integrations for a ProjectManagement endpoint,
     * even the ones that are not registered yet.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getTasksByProjectManagementId(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]

        val projectManagementId = request.pathVariable("projectManagementId")
        val queryFilter = request.queryParams().toQueryFilter(projectManagementId)
        return service
            .findTasks(queryFilter)
            .map {
                it.convertToDto(requestingContributor as? A6Contributor, apiConfigs, request)
            }.let {
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyAndAwait(it)
            }
    }

    /**
     * Handler for the Get ProjectManagement Tasks Stats endpoint,
     * retrieving a Mono with the requested ProjectManagementTasksStats.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProjectManagementTaskStats(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]

        val projectManagementId = request.pathVariable("projectManagementId")
        val queryFilter = request.queryParams().toQueryFilter(projectManagementId)

        return service
            .resolveProjectManagementTasksStats(filter = queryFilter, requestingContributor = requestingContributor as A6Contributor?)
            .convertToDto(requestingContributor, apiConfigs, request)
            .let {
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(it)
            }
    }

    /**
     * Handler for the Get Single ProjectManagementTask endpoint,
     * retrieving a Mono with the requested ProjectManagementTask.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProjectManagementTask(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val projectManagementTaskId = request.pathVariable("id")
        service.findSingleTask(projectManagementTaskId)?.let {
            val outputProjectManagementTask =
                it.convertToDto(
                    requestingContributor as? A6Contributor,
                    apiConfigs,
                    request,
                )
            return ok()
                .contentType(MediaTypes.HAL_FORMS_JSON)
                .bodyValueAndAwait(outputProjectManagementTask)
        }

        return resolveNotFound("Can't find Project Management", "Project Management")
    }
}

private fun MultiValueMap<String, String>.toQueryFilter(projectManagementIds: String? = null): ListTaskFilter =
    ListTaskFilter(
        projectManagementIds =
            projectManagementIds?.let { listOf(it) }
                ?: get(ProjectsManagementTasksQueryParams.PROJECT_MANAGEMENT_IDS.param)?.flatMap {
                    it.split(
                        ",",
                    )
                },
        recentPeriodDays = getFirst(ProjectsManagementTasksQueryParams.RECENT_PERIOD_DAYS.param)?.toLongOrNull(),
        sortField = getFirst(ProjectsManagementTasksQueryParams.SORT_FIELD.param),
    )
