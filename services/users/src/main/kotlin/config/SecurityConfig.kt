package org.example.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
open class SecurityConfig {

    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/register").permitAll()
                it.requestMatchers("/auth/login").permitAll()
                it.requestMatchers("/token/refresh").permitAll()
                it.requestMatchers("/token/public-jwt").permitAll()
                it.anyRequest().authenticated()
            }
        return http.build()
    }
}
