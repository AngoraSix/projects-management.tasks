package com.angorasix.projects.management.tasks.infrastructure.service

import com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService
import com.angorasix.projects.management.tasks.domain.task.TaskRepository
import com.angorasix.projects.management.tasks.infrastructure.applicationevents.ApplicationEventsListener
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.amqp.AmqpConfigurations
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.messaging.listener.handler.ProjectsManagementTasksMessagingHandler
import com.angorasix.projects.management.tasks.messaging.publisher.MessagePublisher
import com.angorasix.projects.management.tasks.presentation.handler.ProjectManagementTasksHandler
import com.angorasix.projects.management.tasks.presentation.router.ProjectManagementTasksRouter
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration {
    @Bean
    fun tasksService(
        repository: TaskRepository,
        applicationEventPublisher: ApplicationEventPublisher,
    ): ProjectsManagementTasksService = ProjectsManagementTasksService(repository, applicationEventPublisher)

    @Bean
    fun tasksHandler(
        service: ProjectsManagementTasksService,
        apiConfigs: ApiConfigs,
    ) = ProjectManagementTasksHandler(service, apiConfigs)

    @Bean
    fun messagingHandler(
        service: ProjectsManagementTasksService,
        applicationEventPublisher: ApplicationEventPublisher,
    ) = ProjectsManagementTasksMessagingHandler(service, applicationEventPublisher)

    @Bean
    fun tasksRouter(
        handler: ProjectManagementTasksHandler,
        apiConfigs: ApiConfigs,
    ) = ProjectManagementTasksRouter(handler, apiConfigs).mgmtTasksRouterFunction()

    @Bean
    fun messagePublisher(
        streamBridge: StreamBridge,
        amqpConfigs: AmqpConfigurations,
    ) = MessagePublisher(streamBridge, amqpConfigs)

    @Bean
    fun applicationEventsListener(messagePublisher: MessagePublisher) = ApplicationEventsListener(messagePublisher)
}
