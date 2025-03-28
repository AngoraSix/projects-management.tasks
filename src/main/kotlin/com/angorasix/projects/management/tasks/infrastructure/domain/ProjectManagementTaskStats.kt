package com.angorasix.projects.management.tasks.infrastructure.domain

data class ProjectManagementTaskStats(
    val projectManagementId: String,
    val project: ProjectStats,
    val contributor: ContributorStats? = null,
)

data class ProjectStats(
    val tasks: TasksStats,
    val contributors: List<ContributorStats> = emptyList(),
)

data class TasksStats(
    val recentlyCompletedCount: Int,
    val completedCount: Int,
    val totalCount: Int,
    val totalEffort: Double,
    val totalDoneEffort: Double,
)

data class ContributorStats(
    val contributorId: String,
    val tasks: TasksStats,
)
