package com.angorasix.projects.management.tasks.domain.task

data class TaskEstimations(
    val caps: Double?,
    val strategy: String?,
    val effort: Double?,
    val complexity: Double?,
    val industry: String?,
    val industryModifier: Double?,
    val moneyPayment: Double?,
)
