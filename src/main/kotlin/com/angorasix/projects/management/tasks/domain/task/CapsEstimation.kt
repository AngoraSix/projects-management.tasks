package com.angorasix.projects.management.tasks.domain.task

data class CapsEstimation(
    val estimatedCaps: Double,
    val effort: Double? = null,
    val difficulty: Double? = null,
    val modifier: Double? = null,
)
