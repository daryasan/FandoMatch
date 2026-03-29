package org.example.config.client

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.api.UserApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
open class UsersClientConfig {
    @Bean
    @Profile("!test")
    open fun tokenApi(@Value("\${clients.users-service.url}") baseUrl: String): TokenApi {
        return TokenApi(baseUrl)
    }

    @Bean
    open fun userApi(@Value("\${clients.users-service.url}") baseUrl: String): UserApi {
        return UserApi(baseUrl)
    }
}
