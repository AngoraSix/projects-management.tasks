package com.angorasix.projects.management.tasks.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.taskaccounting.TaskAccounting
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import com.angorasix.projects.management.tasks.presentation.dto.ProjectsManagementTasksQueryParams
import com.angorasix.projects.management.tasks.presentation.dto.TaskAccountingDto
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.MediaTypes
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.net.URI

/**
 * ProjectManagementTask Handler (Controller) containing all handler functions related to ProjectManagementTask endpoints.
 *
 * @author rozagerardo
 */
class ProjectManagementTasksHandler(
    private val service: ProjectsManagementTasksService,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Handler for the List ProjectManagementTasks endpoint,
     * retrieving a Flux including all persisted ProjectManagementTasks.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun listProjectManagementTasks(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        return service.findTasks(request.queryParams().toQueryFilter()).map {
            it.convertToDto(requestingContributor as? SimpleContributor, apiConfigs, request)
        }.let {
            ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyAndAwait(it)
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
                    requestingContributor as? SimpleContributor,
                    apiConfigs,
                    request,
                )
            return ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProjectManagementTask)
        }

        return resolveNotFound("Can't find Project Management", "Project Management")
    }

    /**
     * Handler for the Create ProjectManagementTasks endpoint, to create a new ProjectManagementTask entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun createProjectManagementTask(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]

        return if (requestingContributor is SimpleContributor) {
            val project = try {
                request.awaitBody<TaskDto>()
                    .convertToDomain(
                        setOf(
                            SimpleContributor(
                                requestingContributor.contributorId,
                                emptySet(),
                            ),
                        ),
                    )
            } catch (e: IllegalArgumentException) {
                return resolveBadRequest(
                    e.message ?: "Incorrect Project Management body",
                    "Project Management",
                )
            }

            val outputProjectManagementTask = service.createTask(project)
                .convertToDto(requestingContributor, apiConfigs, request)

            val selfLink =
                outputProjectManagementTask.links.getRequiredLink(IanaLinkRelations.SELF).href

            created(URI.create(selfLink)).contentType(MediaTypes.HAL_FORMS_JSON)
                .bodyValueAndAwait(outputProjectManagementTask)
        } else {
            resolveBadRequest("Invalid Contributor Token", "Contributor Token")
        }
    }

    /**
     * Handler for the Update ProjectManagementTask endpoint, retrieving a Mono with the updated ProjectManagementTask.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun updateProjectManagementTask(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]

        return if (requestingContributor is SimpleContributor) {
            val projectId = request.pathVariable("id")

            val updateProjectManagementTaskData = try {
                request.awaitBody<TaskDto>()
                    .let { it.convertToDomain(it.admins ?: emptySet()) }
            } catch (e: IllegalArgumentException) {
                return resolveBadRequest(
                    e.message ?: "Incorrect Project Management body",
                    "Project Management",
                )
            }

            service.updateTask(
                projectId,
                updateProjectManagementTaskData,
                requestingContributor,
            )?.let {
                val outputProjectManagementTask =
                    it.convertToDto(
                        requestingContributor,
                        apiConfigs,
                        request,
                    )

                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProjectManagementTask)
            } ?: resolveNotFound("Can't update this project management", "Project Management")
        } else {
            resolveBadRequest("Invalid Contributor Token", "Contributor Token")
        }
    }
}

private fun Task.convertToDto(): TaskDto =
    TaskDto(projectManagementId, admins, assignees, accounting?.convertToDto(), )

private fun Task.convertToDto(
    requestingContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): TaskDto =
    convertToDto().resolveHypermedia(requestingContributor, apiConfigs, request)

private fun TaskDto.convertToDomain(
    admins: Set<SimpleContributor>,
): Task {
    if (projectManagementId == null) {
        throw IllegalArgumentException(
            "Invalid ProjectManagementTask -" +
                "projectManagementId: $projectManagementId"
        )
    }
    return Task(
        projectManagementId,
        admins,
        assignees,
        accounting?.convertToDomain(),
    )
}

private fun TaskAccounting.convertToDto(): TaskAccountingDto {
    return TaskAccountingDto(earnedCaps, redemptionStartInstant, redemptionEndInstant, redemptionFrequency, id)
}

private fun TaskAccountingDto.convertToDomain(): TaskAccounting {
    if (earnedCaps == null || redemptionStartInstant == null || redemptionEndInstant == null) {
        throw IllegalArgumentException(
            "Invalid TaskAccounting -" +
                    "earnedCaps: $earnedCaps -" +
                    "instalmentStartInstant: $redemptionStartInstant -" +
                    "instalmentEndInstant: $redemptionEndInstant",
        )
    }
    return TaskAccounting(earnedCaps, redemptionStartInstant, redemptionEndInstant, emptyList()
    )
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListTaskFilter {
    return ListTaskFilter(
        get(ProjectsManagementTasksQueryParams.PROJECT_MANAGEMENT_IDS.param)?.flatMap { it.split(",") },
    )
}
