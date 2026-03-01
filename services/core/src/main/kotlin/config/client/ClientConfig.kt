package org.example.config.client

import com.fandomatch.users.api.TokenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UsersClientConfig {
    @Bean
    open fun tokenApi(@Value("\${clients.users-service.url}") baseUrl: String): TokenApi {
        return TokenApi(baseUrl)
    }
}
