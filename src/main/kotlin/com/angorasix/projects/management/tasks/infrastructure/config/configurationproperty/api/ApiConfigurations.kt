package com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.api

import com.angorasix.commons.infrastructure.config.configurationproperty.api.Route
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "configs.api")
data class ApiConfigs(

    @NestedConfigurationProperty
    var routes: RoutesConfigs,

    @NestedConfigurationProperty
    var basePaths: BasePathConfigs,
)

data class BasePathConfigs(val projectManagementTasks: String)

data class RoutesConfigs(
    val baseListCrudRoute: String,
    val baseByIdCrudRoute: String,
    val baseByProjectManagementIdCrudRoute: String,
    val listTasksByProjectManagementId: Route,
    val getTask: Route,
)
