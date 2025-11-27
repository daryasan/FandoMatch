package org.example.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties
open class Routes {

    @Value("\${routes.url.users}")
    private lateinit var usersServiceUrl: String

    @Bean
    open fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("users-auth") { r ->
                r.path("/auth/**")
                    .filters { f ->
                        f.stripPrefix(0)
                    }
                    .uri(usersServiceUrl)
            }
            .build()
    }
}