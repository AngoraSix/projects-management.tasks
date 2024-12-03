package com.angorasix.projects.management.tasks.messaging.handler

import com.angorasix.commons.domain.DetailedContributor
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraBulkResourceDto
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraTaskDto
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.dto.syncing.A6InfraBulkSyncingCorrespondenceDto
import com.angorasix.commons.infrastructure.intercommunication.dto.syncing.A6InfraSyncingCorrespondenceDto
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

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
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun tasksSyncing(message: A6InfraMessageDto) = runBlocking {
        if (message.topic == A6InfraTopics.TASKS_INTEGRATION_FULL_SYNCING.value &&
            message.targetType == A6DomainResource.ProjectManagement &&
            message.objectType == A6DomainResource.IntegrationSourceSync.value
        ) {
            val projectManagementId = message.targetId
            val requestingContributor = message.requestingContributor
            val infraTaskDtos = message.extractInfraTaskDtos(objectMapper)

            val infraTasks =
                infraTaskDtos.map { it.toDomain(projectManagementId, requestingContributor) }

            val persistedTasks = projectsManagementTasksService.processTasks(
                infraTasks,
            )

            publishSyncTasksCorrespondence(
                persistedTasks,
                amqpConfigs.bindings.syncingCorrespondence,
                message.objectId,
                projectManagementId,
                requestingContributor,
            )
        }
    }

    private fun publishSyncTasksCorrespondence(
        persistedTasks: List<Task>,
        bindingKey: String,
        sourceSyncId: String,
        projectManagementId: String,
        requestingContributor: DetailedContributor,
    ) {
        if (persistedTasks.isNotEmpty()) {
            val messageData = A6InfraBulkSyncingCorrespondenceDto(
                A6InfraTaskDto::class.java.name,
                A6DomainResource.Task,
                persistedTasks.map {
                    requireNotNull(it.integrationId)
                    requireNotNull(it.id)
                    A6InfraSyncingCorrespondenceDto(it.integrationId, it.id)
                },
            )
            streamBridge.send(
                bindingKey,
                MessageBuilder.withPayload(
                    A6InfraMessageDto(
                        sourceSyncId,
                        A6DomainResource.IntegrationSourceSync,
                        projectManagementId,
                        A6DomainResource.ProjectManagement.value,
                        A6InfraTopics.TASKS_INTEGRATION_SYNCING_CORRESPONDENCE.value,
                        requestingContributor,
                        messageData.toMap(),
                    ),
                ).build(),
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
        null,
        integrationId,
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
