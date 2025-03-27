package com.angorasix.projects.management.tasks.presentation.router

import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.tasks.presentation.handler.ProjectManagementTasksHandler
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all Project Management Tasks related endpoints.
 *
 * @author rozagerardo
 */
class ProjectManagementTasksRouter(
    private val handler: ProjectManagementTasksHandler,
    private val apiConfigs: ApiConfigs,
) {
    /**
     *
     * Main RouterFunction configuration for all endpoints related to ProjectManagements.
     *
     * @return the [RouterFunction] with all the routes for ProjectManagements
     */
    fun mgmtTasksRouterFunction() =
        coRouter {
            apiConfigs.basePaths.projectManagementTasks.nest {
                filter { request, next ->
                    extractRequestingContributor(
                        request,
                        next,
                    )
                }
                apiConfigs.basePaths.baseByProjectManagementIdCrudRoute.nest {
                    defineByProjectManagementIdRoutes()
                }
            }
        }

    private fun CoRouterFunctionDsl.defineByProjectManagementIdRoutes() {
        method(
            apiConfigs.routes.listTasksByProjectManagementId.method,
            handler::getTasksByProjectManagementId,
        )

        path(apiConfigs.routes.getProjectManagementTaskStats.path).nest {
            method(
                apiConfigs.routes.getProjectManagementTaskStats.method,
                handler::getProjectManagementTaskStats,
            )
        }
    }
}
