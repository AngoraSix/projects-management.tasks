package com.angorasix.projects.management.tasks.infrastructure.applicationevents

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.integrations.IntegrationTaskReceived
import com.angorasix.projects.management.tasks.domain.task.Task

class TasksMatchingApplicationEvent(
    val projectManagementId: String,
    val correspondingObjectId: String,
    val persistedTasks: List<Task>,
    val integrationTasks: List<IntegrationTaskReceived.IntegrationTask>,
    val requestingContributor: A6Contributor,
)
