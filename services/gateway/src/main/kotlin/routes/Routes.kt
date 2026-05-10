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

    @Value("\${routes.url.core}")
    private lateinit var coreServiceUrl: String

    @Value("\${routes.url.messaging}")
    private lateinit var messagingServiceUrl: String

    @Bean
    open fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()

            // USERS SERVICE
            .route("users-auth") { r ->
                r.path("/auth/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(usersServiceUrl)
            }
            .route("users-token") { r ->
                r.path("/token/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(usersServiceUrl)
            }
            .route("users-users") { r ->
                r.path("/users/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(usersServiceUrl)
            }

            // CORE SERVICE
            .route("core-user") { r ->
                r.path("/core/user/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(coreServiceUrl)
            }
            .route("core-fandoms") { r ->
                r.path("/core/fandoms/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(coreServiceUrl)
            }
            .route("core-posts") { r ->
                r.path("/core/posts/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(coreServiceUrl)
            }
            .route("core-match") { r ->
                r.path("/core/match/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(coreServiceUrl)
            }
            .route("core-feed") { r ->
                r.path("/core/feed/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(coreServiceUrl)
            }
            .route("core-cities") { r ->
                r.path("/core/cities/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(coreServiceUrl)
            }

            // MESSAGING SERVICE
            .route("messaging") { r ->
                r.path("/messaging/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(messagingServiceUrl)
            }

            // MEDIA
            .route("media") { r ->
                r.path("/media/**")
                    .filters { f -> f.stripPrefix(0) }
                    .uri(messagingServiceUrl)
            }

            .build()
    }
}
