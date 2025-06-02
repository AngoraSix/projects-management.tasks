package com.angorasix.projects.management.tasks.messaging.publisher

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
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
}
