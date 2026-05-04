package com.fandomatch.notifications

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("notifications.email")
data class EmailConfig(
    val from: String = "noreply@fandomatch.com"
)
