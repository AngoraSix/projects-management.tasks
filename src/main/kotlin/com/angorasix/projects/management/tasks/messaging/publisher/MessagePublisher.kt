package com.angorasix.projects.management.tasks.messaging.publisher

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.A6_INFRA_BULK_ID
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.tasks.TasksClosed
import com.angorasix.commons.infrastructure.intercommunication.tasks.TasksSyncingCorrespondenceProcessed
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

class MessagePublisher(
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun publishSyncTasksCorrespondence(
        correspondence: TasksSyncingCorrespondenceProcessed,
        projectManagementId: String,
        correspondingObjectId: String,
        requestingContributor: A6Contributor,
    ) {
        if (correspondence.collection.isNotEmpty()) {
            streamBridge.send(
                amqpConfigs.bindings.mgmtTasksSyncing,
                MessageBuilder
                    .withPayload(
                        A6InfraMessageDto(
                            correspondingObjectId,
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

    fun publishTasksDone(
        tasksClosed: TasksClosed,
        projectManagementId: String,
        requestingContributor: A6Contributor,
    ) {
        if (tasksClosed.collection.isNotEmpty()) {
            streamBridge.send(
                amqpConfigs.bindings.tasksClosed,
                MessageBuilder
                    .withPayload(
                        A6InfraMessageDto(
                            targetId = projectManagementId,
                            targetType = A6DomainResource.PROJECT_MANAGEMENT,
                            objectId = A6_INFRA_BULK_ID,
                            objectType = A6DomainResource.TASK.value,
                            topic = A6InfraTopics.TASKS_CLOSED.value,
                            requestingContributor = requestingContributor,
                            messageData = tasksClosed,
                        ),
                    ).build(),
            )
        }
    }
}
