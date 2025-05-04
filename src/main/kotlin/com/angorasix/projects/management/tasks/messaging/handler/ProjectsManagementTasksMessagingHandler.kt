package com.angorasix.projects.management.tasks.messaging.handler

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.integrations.IntegrationTaskReceived
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.tasks.TasksSyncingCorrespondenceProcessed
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskEstimations
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import kotlinx.coroutines.runBlocking
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksMessagingHandler(
    private val projectsManagementTasksService: ProjectsManagementTasksService,
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun tasksSyncing(message: A6InfraMessageDto<IntegrationTaskReceived>) =
        runBlocking {
            if (message.topic == A6InfraTopics.TASKS_INTEGRATION_FULL_SYNCING.value &&
                message.targetType == A6DomainResource.PROJECT_MANAGEMENT &&
                message.objectType == A6DomainResource.INTEGRATION_SOURCE_SYNC_EVENT.value
            ) {
                val projectManagementId = message.targetId
                val requestingContributor = message.requestingContributor
                val integrationTasks = message.messageData.collection

                val infraTasks =
                    integrationTasks.map { it.toDomain(projectManagementId, requestingContributor) }

                val persistedTasks =
                    projectsManagementTasksService.processTasks(
                        infraTasks,
                    )

                if (integrationTasks.size != persistedTasks.size) {
                    error("Mismatch in number of tasks for [${message.objectId}]")
                }

                val correspondence =
                    TasksSyncingCorrespondenceProcessed(
                        persistedTasks.zip(integrationTasks) { persistedTask, messageTaskDto ->
                            requireNotNull(persistedTask.id)
                            TasksSyncingCorrespondenceProcessed.TaskSyncingCorrespondence(messageTaskDto.integrationId, persistedTask.id)
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
        correspondence: TasksSyncingCorrespondenceProcessed,
        bindingKey: String,
        objectId: String, // Trello-wi7feDfZ
        projectManagementId: String,
        requestingContributor: A6Contributor,
    ) {
        if (correspondence.collection.isNotEmpty()) {
            streamBridge.send(
                bindingKey,
                MessageBuilder
                    .withPayload(
                        A6InfraMessageDto(
                            objectId,
                            A6DomainResource.INTEGRATION_SOURCE_SYNC_EVENT,
                            projectManagementId,
                            A6DomainResource.PROJECT_MANAGEMENT.value,
                            A6InfraTopics.TASKS_INTEGRATION_SYNCING_CORRESPONDENCE.value,
                            requestingContributor,
                            correspondence,
                        ),
                    ).build(),
            )
        }
    }
}

private fun IntegrationTaskReceived.IntegrationTask.toDomain(
    projectManagementId: String,
    requestingContributor: A6Contributor,
): Task =
    Task(
        id = a6Id,
        projectManagementId = projectManagementId,
        title = title,
        description = description ?: "",
        admins = setOf(requestingContributor),
        assigneeIds = assigneeIds,
        done = done,
        dueInstant = dueInstant,
        estimations = estimations?.toDomain(),
    )

private fun IntegrationTaskReceived.IntegrationTaskEstimation.toDomain(): TaskEstimations =
    TaskEstimations(
        caps = caps,
        strategy = strategy,
        effort = effort,
        complexity = complexity,
        industry = industry,
        industryModifier = industryModifier,
        moneyPayment = moneyPayment,
    )
//
// private fun A6InfraMessageDto.extractInfraTaskDtos(objectMapper: ObjectMapper): List<A6InfraTaskDto> {
//    val bulkTasks = A6InfraBulkResourceDto.fromMap<A6DomainResource.Task>(messageData)
//
//    val tasksJson = objectMapper.writeValueAsString(bulkTasks.collection)
//    val tasks = objectMapper.readValue(tasksJson, infraTaskDtoListType)
//    return tasks
// }
