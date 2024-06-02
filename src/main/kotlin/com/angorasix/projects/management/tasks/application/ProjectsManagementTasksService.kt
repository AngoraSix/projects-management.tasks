package com.angorasix.projects.management.tasks.application

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.tasks.domain.task.Task
import com.angorasix.projects.management.tasks.domain.task.TaskRepository
import com.angorasix.projects.management.tasks.domain.task.source.Source
import com.angorasix.projects.management.tasks.domain.task.source.SourceTaskRepository
import com.angorasix.projects.management.tasks.infrastructure.persistence.repository.BulkResult
import com.angorasix.projects.management.tasks.infrastructure.queryfilters.ListTaskFilter
import kotlinx.coroutines.flow.Flow

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksService(
    private val repository: TaskRepository,
    private val sourceRepository: SourceTaskRepository,
) {

    suspend fun findSingleTask(id: String): Task? =
        repository.findById(id)

    suspend fun findSingleTaskByProjectId(projectId: String): Task? =
        repository.findByProjectManagementId(projectId)

    fun findTasks(filter: ListTaskFilter): Flow<Task> =
        repository.findUsingFilter(filter)

    suspend fun createTask(projectManagementTask: Task): Task =
        repository.save(projectManagementTask)

//    suspend fun updateTask(
//        id: String,
//        updateData: Task,
//        requestingContributor: SimpleContributor,
//    ): Task? {
//        val projectManagementTaskToUpdate = repository.findByIdForContributor(
//            ListTaskFilter(
//                listOf(updateData.projectManagementId),
//                setOf(requestingContributor.contributorId),
//                listOf(id),
//            ),
//            requestingContributor,
//        )
//
//        return projectManagementTaskToUpdate?.updateWithData(updateData)?.let { repository.save(it) }
//    }

    /**
     * If a Task exists, we update certain fields. If it doesn't we create it.
     * Tasks that are not included are ignored, at least for the moment.
     */
    suspend fun projectManagementTasksBatchUpdate(
        projectManagementId: String,
        source: Source,
        adminContributor: SimpleContributor,
        updatedTasks: List<Task>,
    ): BulkResult {
        // get the task ids
        val sourceTasks = sourceRepository.findBySourceAndProjectManagementId(source.value, projectManagementId)
        val populatedTasks = updatedTasks.map {
            Task(
                sourceTasks.find { source -> source.taskSourceId == it.sourceTaskId }?.taskId,
                projectManagementId, setOf(adminContributor), it.assignees, it.title, it.description, it.estimation,
            )
        }
        return repository.updateOrCreate(populatedTasks)
    }
}
