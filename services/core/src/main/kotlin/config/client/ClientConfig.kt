package org.example.config.client

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.api.UserApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestClient

@Configuration
open class UsersClientConfig {
    @Bean
    @Profile("!test")
    open fun tokenApi(
        @Value("\${clients.users-service.url}") baseUrl: String,
        builder: RestClient.Builder
    ): TokenApi {
        return TokenApi(builder.baseUrl(baseUrl).build())
    }

    @Bean
    open fun userApi(
        @Value("\${clients.users-service.url}") baseUrl: String,
        builder: RestClient.Builder
    ): UserApi {
        return UserApi(builder.baseUrl(baseUrl).build())
    }
}
