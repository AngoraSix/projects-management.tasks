package com.angorasix.projects.management.tasks.infrastructure.applicationevents

import com.angorasix.commons.infrastructure.intercommunication.tasks.TasksClosed
import com.angorasix.commons.infrastructure.intercommunication.tasks.TasksSyncingCorrespondenceProcessed
import com.angorasix.projects.management.tasks.messaging.publisher.MessagePublisher
import org.springframework.context.event.EventListener
import java.time.Instant

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

    @EventListener
    fun handleTasksDone(evt: TasksDoneApplicationEvent) {
        val tasksClosed =
            TasksClosed(
                evt.projectManagementId,
                evt.doneTasks.map { task ->
                    requireNotNull(task.id)
                    TasksClosed.TaskClosed(
                        task.id,
                        task.assigneeIds,
                        task.doneInstant ?: Instant.now(),
                        task.estimations?.moneyPayment,
                        task.estimations?.caps,
                    )
                },
            )
        messagePublisher.publishTasksDone(
            tasksClosed = tasksClosed,
            projectManagementId = evt.projectManagementId,
            requestingContributor = evt.requestingContributor,
        )
    }
}
