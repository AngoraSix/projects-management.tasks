package com.angorasix.projects.management.tasks.application

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskRepository
import com.angorasix.projects.management.tasks.infrastructure.domain.ProjectManagementTaskStats
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import java.time.Instant

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksService(
    private val repository: TaskRepository,
) {
    suspend fun findSingleTask(id: String): Task? = repository.findById(id)

    fun findTasks(filter: ListTaskFilter): Flow<Task> = repository.findUsingFilter(filter)

    suspend fun resolveProjectManagementTasksStats(
        filter: ListTaskFilter,
        requestingContributor: SimpleContributor?,
    ): ProjectManagementTaskStats = repository.resolveStatsUsingFilter(filter, requestingContributor)

    suspend fun createTask(projectManagementTask: Task): Task = repository.save(projectManagementTask)

    /**
     * If a Task exists, we update certain fields. If it doesn't we create it.
     */
    suspend fun processTasks(tasks: List<Task>): List<Task> {
        val updatedTasks: List<Task> =
            tasks.map { task ->
                if (task.id != null) {
                    // For existing tasks, retrieve and merge the update:
                    repository.findById(task.id)?.let { existingTask ->
                        mergeTask(existingTask, task)
                    } ?: run {
                        // If not found, treat as a new task
                        if (task.done) {
                            task.copy(doneInstant = task.doneInstant ?: Instant.now())
                        } else {
                            task.copy(doneInstant = null)
                        }
                    }
                } else {
                    // New task: set doneInstant accordingly
                    if (task.done) {
                        task.copy(doneInstant = task.doneInstant ?: Instant.now())
                    } else {
                        task.copy(doneInstant = null)
                    }
                }
            }
        // Save all updated tasks in a single bulk operation.
        return repository.saveAll(updatedTasks).toList() // Should maintain order
    }

    /**
     * Merges an existing Task with updated values.
     * Rules for doneInstant:
     * - If the existing task already has a non-null doneInstant, keep it.
     * - Otherwise, if update.done is true, set doneInstant to:
     *     • update.doneInstant if provided,
     *     • or the current timestamp if not.
     * - If update.done is false, set doneInstant to null.
     */
    private fun mergeTask(
        existing: Task,
        update: Task,
    ): Task {
        val newDoneInstant: Instant? =
            if (update.done) {
                update.doneInstant ?: existing.doneInstant ?: Instant.now()
            } else {
                null
            }
        return existing.copy(
            title = update.title,
            description = update.description,
            done = update.done,
            doneInstant = newDoneInstant,
            assigneeIds = update.assigneeIds,
            estimations = update.estimations,
            // Include other fields as needed
        )
    }
}
