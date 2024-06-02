package com.angorasix.projects.management.tasks.infrastructure.persistence.repository

data class BulkResult(
    val inserted: Int,
    val modified: Int,
)