package com.angorasix.projects.management.tasks.application

import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskRepository
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksService(
    private val repository: TaskRepository,
) {

    suspend fun findSingleTask(id: String): Task? =
        repository.findById(id)

    fun findTasks(filter: ListTaskFilter): Flow<Task> =
        repository.findUsingFilter(filter)

    suspend fun createTask(projectManagementTask: Task): Task =
        repository.save(projectManagementTask)

    /**
     * If a Task exists, we update certain fields. If it doesn't we create it.
     * Tasks that are not included are ignored, at least for the moment.
     */
    suspend fun processTasks(
        tasks: List<Task>,
    ): List<Task> {
        return repository.saveAll(tasks).toList()
    }
}
