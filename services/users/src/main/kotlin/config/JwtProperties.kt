package org.example.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var accessExpiration: Long = 0,
    var refreshExpirationDays: Long = 0
)
