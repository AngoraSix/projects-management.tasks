package com.angorasix.projects.management.tasks.infrastructure.applicationevents

import com.angorasix.commons.infrastructure.intercommunication.tasks.TasksSyncingCorrespondenceProcessed
import com.angorasix.projects.management.tasks.messaging.publisher.MessagePublisher
import org.springframework.context.event.EventListener

class ApplicationEventsListener(
    private val messagePublisher: MessagePublisher,
) {
    @EventListener
    fun handleTasksMatchingCorrespondence(evt: TasksMatchingApplicationEvent) {
        val correspondence =
            TasksSyncingCorrespondenceProcessed(
                evt.persistedTasks.zip(evt.integrationTasks) { persistedTask, messageTaskDto ->
                    requireNotNull(persistedTask.id)
                    TasksSyncingCorrespondenceProcessed.TaskSyncingCorrespondence(messageTaskDto.integrationId, persistedTask.id)
                },
            )
        messagePublisher.publishSyncTasksCorrespondence(
            correspondence = correspondence,
            projectManagementId = evt.projectManagementId,
            correspondingObjectId = evt.correspondingObjectId,
            requestingContributor = evt.requestingContributor,
        )
    }
}
