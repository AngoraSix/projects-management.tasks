package com.angorasix.projects.management.tasks.presentation.router

import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import com.angorasix.projects.management.core.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.management.core.presentation.handler.ProjectsManagementHandler
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all ProjectManagement related endpoints.
 *
 * @author rozagerardo
 */
class ProjectsManagementTasksRouter(
    private val handler: ProjectsManagementHandler,
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Main RouterFunction configuration for all endpoints related to ProjectManagements.
     *
     * @return the [RouterFunction] with all the routes for ProjectManagements
     */
    fun projectRouterFunction() = coRouter {
        apiConfigs.basePaths.projectsManagement.nest {
            filter { request, next ->
                extractRequestingContributor(
                    request,
                    next,
                )
            }
            apiConfigs.routes.baseByProjectIdCrudRoute.nest {
                method(apiConfigs.routes.createProjectManagementByProjectId.method).nest {
                    method(
                        apiConfigs.routes.createProjectManagementByProjectId.method,
                        handler::createProjectManagementByProjectId,
                    )
                }
                method(apiConfigs.routes.getProjectManagementByProjectId.method).nest {
                    method(
                        apiConfigs.routes.getProjectManagementByProjectId.method,
                        handler::getProjectManagementByProjectId,
                    )
                }
            }
            apiConfigs.routes.baseByIdCrudRoute.nest {
                method(apiConfigs.routes.updateProjectManagement.method).nest {
                    method(
                        apiConfigs.routes.updateProjectManagement.method,
                        handler::updateProjectManagement,
                    )
                }
                method(apiConfigs.routes.getProjectManagement.method).nest {
                    method(
                        apiConfigs.routes.getProjectManagement.method,
                        handler::getProjectManagement,
                    )
                }
            }
            apiConfigs.routes.baseListCrudRoute.nest {
                path(apiConfigs.routes.baseListCrudRoute).nest {
                    method(apiConfigs.routes.createProjectManagement.method).nest {
                        method(
                            apiConfigs.routes.createProjectManagement.method,
                            handler::createProjectManagement,
                        )
                    }
                    method(apiConfigs.routes.listProjectManagements.method).nest {
                        method(
                            apiConfigs.routes.listProjectManagements.method,
                            handler::listProjectManagements,
                        )
                    }
                }
            }
        }
    }
}
