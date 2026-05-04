package com.fandomatch.notifications

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(NotificationsConfig::class, EmailConfig::class)
open class NotificationsAutoConfiguration(private val notificationsConfig: NotificationsConfig) {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ["notifications.firebase.credentials-json"])
    open fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isEmpty()) {
            val credentials = GoogleCredentials.fromStream(
                notificationsConfig.credentialsJson.byteInputStream()
            )
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(notificationsConfig.projectId)
                .build()
            return FirebaseApp.initializeApp(options)
        }
        return FirebaseApp.getInstance()
    }
}
