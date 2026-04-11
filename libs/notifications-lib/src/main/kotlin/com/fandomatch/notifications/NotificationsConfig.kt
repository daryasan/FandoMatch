package com.fandomatch.notifications

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("notifications.firebase")
data class NotificationsConfig(
    val credentialsJson: String,
    val projectId: String
)
