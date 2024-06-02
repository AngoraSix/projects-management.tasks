package com.angorasix.projects.management.tasks.domain.task.source

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * <p>
 *     Data class defining source information for a task.
 * </p>
 *
 * @author rozagerardo
 */
@Document
@CompoundIndex(name = "management_idx", def = "{'source': 1, 'projectManagementId': 1}")
data class SourceTask (
    @field:Id val taskId: String,

//    @Indexed
    val projectManagementId: String,

    val taskSourceId: String?,

    var source: String,
)
