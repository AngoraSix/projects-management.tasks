package com.angorasix.projects.management.tasks.presentation.dto

/**
 *
 *
 * @author rozagerardo
 */
enum class ProjectsManagementTasksQueryParams(
    val param: String,
) {
    PROJECT_MANAGEMENT_IDS("projectManagementIds"),
    RECENT_PERIOD_DAYS("recentPeriodDays"),
}
