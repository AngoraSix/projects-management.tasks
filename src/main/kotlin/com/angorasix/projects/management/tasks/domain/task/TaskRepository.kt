package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.projects.management.core.infrastructure.persistence.repository.ProjectManagementFilterRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

/**
 *
 *
 * @author rozagerardo
 */
interface TaskRepository :
    CoroutineCrudRepository<Task, String>,
    CoroutineSortingRepository<Task, String>,
    ProjectManagementFilterRepository {
    suspend fun findByProjectId(projectId: String): Task?
}
