package com.angorasix.projects.management.tasks.infrastructure.queryfilters

/**
 * <p>
 *     Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListTaskFilter(
    val projectManagementIds: Collection<String>? = null,
    val adminId: Set<String>? = null,
    val ids: Collection<String>? = null, // task ids
    val recentPeriodDays: Long? = null,
    val sortField: String? = null,
)
