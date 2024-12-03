package com.angorasix.projects.management.tasks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack

/**
 * Spring Boot main class for Projects Management.
 *
 * @author rozagerardo
 */
@SpringBootApplication
@EnableHypermediaSupport(
    type = [EnableHypermediaSupport.HypermediaType.HAL_FORMS],
    stacks = [WebStack.WEBFLUX],
)
@ConfigurationPropertiesScan("com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty")
class ProjectsManagementTasksApplication

/**
 * Main application method.
 *
 * @param args java args
 */
fun main(args: Array<String>) {
    runApplication<ProjectsManagementTasksApplication>(args = args)
}
