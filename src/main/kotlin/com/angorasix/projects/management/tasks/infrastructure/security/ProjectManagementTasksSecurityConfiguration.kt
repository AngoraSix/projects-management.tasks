package com.angorasix.projects.management.tasks.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 *
 *
 * All Spring Security configuration.
 *
 *
 * @author rozagerardo
 */
class ProjectManagementTasksSecurityConfiguration {
    /**
     *
     *
     * Security Filter Chain setup.
     *
     *
     * @param http Spring's customizable ServerHttpSecurity bean
     * @return fully configured SecurityWebFilterChain
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange { exchanges: ServerHttpSecurity.AuthorizeExchangeSpec ->
            exchanges
                .pathMatchers(
                    HttpMethod.GET,
                    "/management-core/**",
                ).permitAll()
                .anyExchange().authenticated()
        }.oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }
        return http.build()
    }
}
