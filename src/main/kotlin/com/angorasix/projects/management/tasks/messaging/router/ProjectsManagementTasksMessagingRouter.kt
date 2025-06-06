package com.angorasix.projects.management.tasks.messaging.router

import com.angorasix.commons.infrastructure.intercommunication.integrations.IntegrationTaskReceived
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
import com.angorasix.projects.management.tasks.messaging.handler.ProjectsManagementTasksMessagingHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
class ProjectsManagementTasksMessagingRouter(
    val handler: ProjectsManagementTasksMessagingHandler,
) {
    @Bean
    fun tasksSyncing(): (A6InfraMessageDto<IntegrationTaskReceived>) -> Unit = { handler.tasksSyncing(it) }
}
