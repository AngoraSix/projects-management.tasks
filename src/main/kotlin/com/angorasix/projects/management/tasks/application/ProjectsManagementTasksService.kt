package com.angorasix.projects.management.tasks.application

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskRepository
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import kotlinx.coroutines.flow.Flow

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksService(private val repository: TaskRepository) {

    suspend fun findSingleTask(id: String): Task? =
        repository.findById(id)

    suspend fun findSingleTaskByProjectId(projectId: String): Task? =
        repository.findByProjectManagementId(projectId)

    fun findTasks(filter: ListTaskFilter): Flow<Task> =
        repository.findUsingFilter(filter)

    suspend fun createTask(projectManagementTask: Task): Task =
        repository.save(projectManagementTask)

    suspend fun updateTask(
        id: String,
        updateData: Task,
        requestingContributor: SimpleContributor,
    ): Task? {
        val projectManagementTaskToUpdate = repository.findByIdForContributor(
            ListTaskFilter(
                listOf(updateData.projectManagementId),
                setOf(requestingContributor.contributorId),
                listOf(id),
            ),
            requestingContributor,
        )

        return projectManagementTaskToUpdate?.updateWithData(updateData)?.let { repository.save(it) }
    }

    private fun Task.updateWithData(other: Task): Task {
        this.assignees = other.assignees;
        return this
    }
}
