package com.angorasix.projects.management.tasks.infrastructure.service

import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.TaskRepository
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.messaging.handler.ProjectsManagementTasksMessagingHandler
import com.angorasix.projects.management.tasks.presentation.handler.ProjectManagementTasksHandler
import com.angorasix.projects.management.tasks.presentation.router.ProjectManagementTasksRouter
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration {
    @Bean
    fun tasksService(repository: TaskRepository): ProjectsManagementTasksService = ProjectsManagementTasksService(repository)

    @Bean
    fun tasksHandler(
        service: ProjectsManagementTasksService,
        apiConfigs: ApiConfigs,
    ) = ProjectManagementTasksHandler(service, apiConfigs)

    @Bean
    fun tasksMessagingHandler(
        service: ProjectsManagementTasksService,
        streamBridge: StreamBridge,
        amqpConfigs: AmqpConfigurations,
    ) = ProjectsManagementTasksMessagingHandler(service, streamBridge, amqpConfigs)

    @Bean
    fun tasksRouter(
        handler: ProjectManagementTasksHandler,
        apiConfigs: ApiConfigs,
    ) = ProjectManagementTasksRouter(handler, apiConfigs).mgmtTasksRouterFunction()
}
