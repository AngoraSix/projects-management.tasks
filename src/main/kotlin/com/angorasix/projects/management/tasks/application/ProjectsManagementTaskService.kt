package com.angorasix.projects.management.tasks.application

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.management.core.domain.management.ProjectManagement
import com.angorasix.projects.management.core.domain.management.ProjectManagementRepository
import com.angorasix.projects.management.core.infrastructure.queryfilters.ListProjectsManagementFilter
import kotlinx.coroutines.flow.Flow

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsManagementTaskService(private val repository: ProjectManagementRepository) {

    suspend fun findSingleProjectManagement(id: String): ProjectManagement? =
        repository.findById(id)

    suspend fun findSingleProjectManagementByProjectId(projectId: String): ProjectManagement? =
        repository.findByProjectId(projectId)

    fun findProjectManagements(filter: ListProjectsManagementFilter): Flow<ProjectManagement> =
        repository.findUsingFilter(filter)

    suspend fun createProjectManagement(projectManagement: ProjectManagement): ProjectManagement =
        repository.save(projectManagement)

    suspend fun updateProjectManagement(
        id: String,
        updateData: ProjectManagement,
        requestingContributor: SimpleContributor,
    ): ProjectManagement? {
        val projectManagementToUpdate = repository.findByIdForContributor(
            ListProjectsManagementFilter(
                listOf(updateData.projectId),
                setOf(requestingContributor.contributorId),
                listOf(id),
            ),
            requestingContributor,
        )

        return projectManagementToUpdate?.updateWithData(updateData)?.let { repository.save(it) }
    }

    private fun ProjectManagement.updateWithData(other: ProjectManagement): ProjectManagement {
        this.status = other.status
        return this
    }
}
