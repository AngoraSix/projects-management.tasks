package com.angorasix.projects.management.tasks.messaging.listener.handler

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.integrations.IntegrationTaskReceived
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskEstimations
import com.angorasix.projects.management.tasks.infrastructure.applicationevents.TasksMatchingApplicationEvent
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEventPublisher

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksMessagingHandler(
    private val projectsManagementTasksService: ProjectsManagementTasksService,
    private val applicationEventPublisher: ApplicationEventPublisher,
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
                        tasks = infraTasks,
                        projectManagementId = projectManagementId,
                        requestingContributor = requestingContributor,
                    )

                if (integrationTasks.size != persistedTasks.size) {
                    error("Mismatch in number of tasks for [${message.objectId}]")
                }

                applicationEventPublisher.publishEvent(
                    TasksMatchingApplicationEvent(
                        projectManagementId = projectManagementId,
                        persistedTasks = persistedTasks,
                        integrationTasks = integrationTasks,
                        correspondingObjectId = message.objectId,
                        requestingContributor = requestingContributor,
                    ),
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
