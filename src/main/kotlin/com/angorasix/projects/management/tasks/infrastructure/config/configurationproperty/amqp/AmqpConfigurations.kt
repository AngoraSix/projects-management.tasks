package com.angorasix.projects.management.tasks.infrastructure.config.configurationproperty.amqp

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "configs.amqp")
data class AmqpConfigurations(

    @NestedConfigurationProperty
    var bindings: BindingConfigs,
)

class BindingConfigs(
    val syncingCorrespondence: String,
)
