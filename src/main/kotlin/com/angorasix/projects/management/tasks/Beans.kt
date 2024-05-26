package com.angorasix.projects.management.tasks

import com.angorasix.projects.management.tasks.infrastructure.security.ProjectManagementTasksSecurityConfiguration
import com.angorasix.projects.management.tasks.presentation.handler.ProjectManagementTasksHandler
import com.angorasix.projects.management.tasks.presentation.router.ProjectManagementTasksRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

val beans = beans {
    bean {
        ProjectManagementTasksSecurityConfiguration().springSecurityFilterChain(ref())
    }
    bean<com.angorasix.projects.management.tasks.application.ProjectsManagementTasksService>()
    bean<ProjectManagementTasksHandler>()
    bean {
        ProjectManagementTasksRouter(ref(), ref()).projectRouterFunction()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = com.angorasix.projects.management.tasks.beans.initialize(context)
}