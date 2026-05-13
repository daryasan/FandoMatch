package org.example.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
open class SecurityConfig(
    private val jwtAuthFilterConfig: JwtAuthFilterConfig
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    org.springframework.http.HttpMethod.POST,
                    "/core/posts/get",
                    "/core/fandoms/user",
                    "/core/fandoms/request-new"
                ).permitAll()
                it.requestMatchers(
                    org.springframework.http.HttpMethod.GET,
                    "/core/posts/{post_id}",
                    "/core/posts/{post_id}/comments",
                    "/core/fandoms/all",
                    "/core/fandoms/categories"
                ).permitAll()
                it.requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                it.requestMatchers("/core/match/internal/**").permitAll()
                it.requestMatchers("/error").permitAll()
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .addFilterBefore(
                jwtAuthFilterConfig,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}
