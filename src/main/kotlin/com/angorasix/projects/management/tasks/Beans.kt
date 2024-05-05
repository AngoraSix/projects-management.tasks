package com.angorasix.projects.management.tasks

import com.angorasix.projects.management.tasks.infrastructure.security.ProjectsManagementTasksSecurityConfiguration
import com.angorasix.projects.management.tasks.presentation.handler.ProjectsManagementTasksHandler
import com.angorasix.projects.management.tasks.presentation.router.ProjectsManagementTasksRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

val beans = beans {
    bean {
        ProjectsManagementTasksSecurityConfiguration().springSecurityFilterChain(ref())
    }
    bean<com.angorasix.projects.management.tasks.application.ProjectsManagementTaskService>()
    bean<ProjectsManagementTasksHandler>()
    bean {
        ProjectsManagementTasksRouter(ref(), ref()).projectRouterFunction()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = com.angorasix.projects.management.tasks.beans.initialize(context)
}
