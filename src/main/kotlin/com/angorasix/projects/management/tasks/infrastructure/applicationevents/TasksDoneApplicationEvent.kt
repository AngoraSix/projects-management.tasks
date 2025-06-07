package com.angorasix.projects.management.tasks.infrastructure.applicationevents

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.projects.management.tasks.domain.task.Task

class TasksDoneApplicationEvent(
    val projectManagementId: String,
    val doneTasks: List<Task>,
    val requestingContributor: A6Contributor,
)
