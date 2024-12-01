package com.angorasix.projects.management.tasks.messaging.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraBulkResourceDto
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraTaskDto
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.Task
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
val infraTaskDtoListType = object : TypeReference<List<A6InfraTaskDto>>() {}

class ProjectsManagementTasksMessagingHandler(
    private val projectsManagementTasksService: ProjectsManagementTasksService,
    private val objectMapper: ObjectMapper,
) {
    fun tasksSyncing(message: A6InfraMessageDto) = runBlocking {
        if (message.topic == A6InfraTopics.TASKS_INTEGRATION_FULL_SYNCING.value &&
            message.targetType == A6DomainResource.ProjectManagement &&
            message.objectType == A6DomainResource.IntegrationSourceSync.value
        ) {
            println("GER2")
            val projectManagementId = message.targetId
            val requestingContributor = message.requestingContributor
            val infraTaskDtos = message.extractInfraTaskDtos(objectMapper)
            val tasks =
                infraTaskDtos.map { it.toDomain(projectManagementId, requestingContributor) }

            projectsManagementTasksService.processTasks(
                tasks,
            )
        }
    }
}

private fun A6InfraTaskDto.toDomain(
    projectManagementId: String,
    requestingContributor: SimpleContributor,
): Task {
    return Task(
        angorasixId,
        projectManagementId,
        title,
        description ?: "",
        setOf(requestingContributor),
        assigneeIds,
        done,
        dueInstant,
    )
}

private fun A6InfraMessageDto.extractInfraTaskDtos(
    objectMapper: ObjectMapper,
): List<A6InfraTaskDto> {
    val bulkTasks = A6InfraBulkResourceDto.fromMap<A6DomainResource.Task>(messageData)

    val tasksJson = objectMapper.writeValueAsString(bulkTasks.collection)
    val tasks = objectMapper.readValue(tasksJson, infraTaskDtoListType)

    return tasks
}
