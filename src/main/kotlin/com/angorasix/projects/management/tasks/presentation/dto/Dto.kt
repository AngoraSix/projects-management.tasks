package com.angorasix.projects.management.tasks.presentation.dto

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.integrations.SourcedContributorWrapper
import org.springframework.hateoas.RepresentationModel
import java.time.Instant

/**
 *
 *
 * @author rozagerardo
 */
data class TaskDto(
    val projectManagementId: String? = null,
    val admins: Set<SimpleContributor>? = mutableSetOf(),
    val assignees: Set<SourcedContributorWrapper>? = mutableSetOf(),
    val title: String,
    val description: String = "",
    val estimation: CapsEstimationDto?,
    val sourceTaskId: String? = null,
    val id: String? = null,
) : RepresentationModel<TaskDto>()

data class CapsEstimationDto(
    val estimatedCaps: Double,
    val effort: Double? = null,
    val difficulty: Double? = null,
    val modifier: Double? = null,
)

data class TaskAccountingDto(
    val taskId: String? = null,
    val earnedCaps: Double? = null,
    val redemptionStartInstant: Instant? = null,
    val redemptionEndInstant: Instant? = null,
    val redemptionFrequency: String? = null,
    val id: String? = null,
)
