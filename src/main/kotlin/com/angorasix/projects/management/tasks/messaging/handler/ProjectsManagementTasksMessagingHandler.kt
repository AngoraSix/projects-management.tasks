package com.angorasix.projects.management.tasks.messaging.handler

import com.angorasix.commons.domain.DetailedContributor
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraBulkResourceDto
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraTaskDto
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraTaskEstimationDto
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.dto.syncing.A6InfraBulkSyncingCorrespondenceDto
import com.angorasix.commons.infrastructure.intercommunication.dto.syncing.A6InfraSyncingCorrespondenceDto
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskEstimations
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
    fun tasksSyncing(message: A6InfraMessageDto) =
        runBlocking {
            if (message.topic == A6InfraTopics.TASKS_INTEGRATION_FULL_SYNCING.value &&
                message.targetType == A6DomainResource.ProjectManagement &&
                message.objectType == A6DomainResource.IntegrationSourceSync.value
            ) {
                val projectManagementId = message.targetId
                val requestingContributor = message.requestingContributor
                val infraTaskDtos = message.extractInfraTaskDtos(objectMapper)

                val infraTasks =
                    infraTaskDtos.map { it.toDomain(projectManagementId, requestingContributor) }

                val persistedTasks =
                    projectsManagementTasksService.processTasks(
                        infraTasks,
                    )

                if (infraTaskDtos.size != persistedTasks.size) {
                    error("Mismatch in number of tasks for [${message.objectId}]")
                }

                val correspondence =
                    A6InfraBulkSyncingCorrespondenceDto(
                        A6InfraTaskDto::class.java.name,
                        A6DomainResource.Task,
                        persistedTasks.zip(infraTaskDtos) { persistedTask, messageTaskDto ->
                            requireNotNull(persistedTask.id)
                            A6InfraSyncingCorrespondenceDto(messageTaskDto.integrationId, persistedTask.id)
                        },
                    )

                publishSyncTasksCorrespondence(
                    correspondence,
                    amqpConfigs.bindings.mgmtTasksSyncing,
                    message.objectId,
                    projectManagementId,
                    requestingContributor,
                )
            }
        }

    private fun publishSyncTasksCorrespondence(
        correspondence: A6InfraBulkSyncingCorrespondenceDto,
        bindingKey: String,
        objectId: String, // Trello-wi7feDfZ
        projectManagementId: String,
        requestingContributor: DetailedContributor,
    ) {
        if (correspondence.collection.isNotEmpty()) {
            streamBridge.send(
                bindingKey,
                MessageBuilder
                    .withPayload(
                        A6InfraMessageDto(
                            objectId,
                            A6DomainResource.IntegrationSourceSync,
                            projectManagementId,
                            A6DomainResource.ProjectManagement.value,
                            A6InfraTopics.TASKS_INTEGRATION_SYNCING_CORRESPONDENCE.value,
                            requestingContributor,
                            correspondence.toMap(),
                        ),
                    ).build(),
            )
        }
    }
}

private fun A6InfraTaskDto.toDomain(
    projectManagementId: String,
    requestingContributor: SimpleContributor,
): Task =
    Task(
        id = angorasixId,
        projectManagementId = projectManagementId,
        title = title,
        description = description ?: "",
        admins = setOf(requestingContributor),
        assigneeIds = assigneeIds,
        done = done,
        dueInstant = dueInstant,
        estimations = estimations?.toDomain(),
    )

private fun A6InfraTaskEstimationDto.toDomain(): TaskEstimations =
    TaskEstimations(
        caps = caps,
        strategy = strategy,
        effort = effort,
        complexity = complexity,
        industry = industry,
        industryModifier = industryModifier,
        moneyPayment = moneyPayment,
    )

private fun A6InfraMessageDto.extractInfraTaskDtos(objectMapper: ObjectMapper): List<A6InfraTaskDto> {
    val bulkTasks = A6InfraBulkResourceDto.fromMap<A6DomainResource.Task>(messageData)

    val tasksJson = objectMapper.writeValueAsString(bulkTasks.collection)
    val tasks = objectMapper.readValue(tasksJson, infraTaskDtoListType)
    return tasks
}
