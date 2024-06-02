package com.angorasix.projects.management.tasks.domain.task.source

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

/**
 *
 * Source Task Repository.
 *
 * @author rozagerardo
 */
interface SourceTaskRepository :
    CoroutineCrudRepository<SourceTask, String>,
    CoroutineSortingRepository<SourceTask, String> {
    fun findBySourceAndProjectManagementId(source: String, projectManagementId: String): List<SourceTask>
}
