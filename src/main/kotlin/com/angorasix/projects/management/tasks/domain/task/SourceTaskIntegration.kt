package com.angorasix.projects.management.tasks.domain.task

import org.springframework.data.mongodb.core.mapping.Document

/**
 * <p>
 *     Data class defining source information for task.
 * </p>
 *
 * @author rozagerardo
 */
@Document
data class SourceTaskIntegration (
    val taskSourceId: String?,
    var source: Source,
)
