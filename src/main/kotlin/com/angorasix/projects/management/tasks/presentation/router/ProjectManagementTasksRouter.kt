package com.angorasix.projects.management.tasks.presentation.router

import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.presentation.handler.ProjectManagementTasksHandler
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all ProjectManagement related endpoints.
 *
 * @author rozagerardo
 */
class ProjectManagementTasksRouter(
    private val handler: ProjectManagementTasksHandler,
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Main RouterFunction configuration for all endpoints related to ProjectManagements.
     *
     * @return the [RouterFunction] with all the routes for ProjectManagements
     */
    fun projectRouterFunction() = coRouter {
        apiConfigs.basePaths.projectsManagementTasks.nest {
            filter { request, next ->
                extractRequestingContributor(
                    request,
                    next,
                )
            }
            apiConfigs.routes.baseByIdCrudRoute.nest {
                method(apiConfigs.routes.updateTask.method).nest {
                    method(
                        apiConfigs.routes.updateTask.method,
                        handler::updateProjectManagementTask,
                    )
                }
                method(apiConfigs.routes.getTask.method).nest {
                    method(
                        apiConfigs.routes.getTask.method,
                        handler::getProjectManagementTask,
                    )
                }
            }
            apiConfigs.routes.baseListCrudRoute.nest {
                path(apiConfigs.routes.baseListCrudRoute).nest {
                    method(apiConfigs.routes.createTask.method).nest {
                        method(
                            apiConfigs.routes.createTask.method,
                            handler::createProjectManagementTask,
                        )
                    }
                    method(apiConfigs.routes.listTasks.method).nest {
                        method(
                            apiConfigs.routes.listTasks.method,
                            handler::listProjectManagementTasks,
                        )
                    }
                }
            }
        }
    }
}
