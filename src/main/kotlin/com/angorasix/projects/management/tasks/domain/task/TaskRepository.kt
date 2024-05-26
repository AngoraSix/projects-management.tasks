package com.angorasix.projects.management.tasks.domain.task

import com.angorasix.projects.management.tasks.infrastructure.persistence.repository.TaskFilterRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

/**
 *
 * Project Management Task Repository.
 *
 * @author rozagerardo
 */
interface TaskRepository :
    CoroutineCrudRepository<Task, String>,
    CoroutineSortingRepository<Task, String>,
    TaskFilterRepository {
    suspend fun findByProjectManagementId(projectId: String): Task?
}
