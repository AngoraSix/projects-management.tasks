package com.angorasix.notifications.messaging.handler

import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.messaging.dto.A6InfraMessageDto
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.source.Source
import com.angorasix.projects.management.tasks.presentation.dto.TaskDto
import com.angorasix.projects.management.tasks.presentation.handler.convertToDomain
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking

val taskDtoListType = object : TypeReference<List<TaskDto>>() {}

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksMessagingHandler(
    private val projectsManagementTasksService: ProjectsManagementTasksService,
    private val objectMapper: ObjectMapper,
) {
    fun projectManagementTasksUpdate(message: A6InfraMessageDto) = runBlocking {
        val source = getSourceFromValue(message.objectId)
        if (message.topic == A6InfraTopics.MGMT_TASKS_UPDATE.value && message.targetType == A6DomainResource.PROJECT_MANAGEMENT && message.objectType == A6DomainResource.PROJECT_MANAGEMENT_INTEGRATION_SOURCE.value && source != null) {

            val projectManagementId = message.targetId
            val requestingContributor = message.requestingContributor
            val tasks = message.extractTaskDtos(objectMapper, projectManagementId)
                .map { it.convertToDomain(setOf(requestingContributor)) }
            projectsManagementTasksService.projectManagementTasksBatchUpdate(projectManagementId, source, requestingContributor, tasks)//?.launchIn(this) // required
        }
    }
}


private fun A6InfraMessageDto.extractTaskDtos(objectMapper: ObjectMapper, projectManagementId: String): List<TaskDto> {
    val tasksJson = objectMapper.writeValueAsString(messageData["tasks"])
    val partiallyPopulatedTasks = objectMapper.readValue(tasksJson, taskDtoListType)
    return partiallyPopulatedTasks.map {
        TaskDto(
            projectManagementId,
            emptySet(),
            it.assignees,
            it.title,
            it.description,
            it.estimation,
            it.sourceTaskId,
            null,
        )
    }
}

private fun getSourceFromValue(value: String) =
    Source.values().find { it.value.equals(value, ignoreCase = true) }

