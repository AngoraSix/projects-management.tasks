package com.angorasix.projects.management.tasks.presentation.dto

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.domain.SourcedContributorWrapper
import org.springframework.hateoas.RepresentationModel
import java.time.Instant

/**
 *
 *
 * @author rozagerardo
 */
data class TaskDto(
    val projectManagementId: String? = null,
    var admins: Set<SimpleContributor>? = mutableSetOf(),
    var assignees: Set<SourcedContributorWrapper> = mutableSetOf(),
    val accounting: TaskAccountingDto? = null,
    val id: String? = null,
) : RepresentationModel<TaskDto>()

data class TaskAccountingDto(
    val earnedCaps: Double? = null,
    val redemptionStartInstant: Instant? = null,
    val redemptionEndInstant: Instant? = null,
    val redemptionFrequency: String? = null,
    val id: String? = null,
)
