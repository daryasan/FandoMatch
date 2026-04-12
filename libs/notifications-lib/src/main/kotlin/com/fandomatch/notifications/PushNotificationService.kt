package com.fandomatch.notifications

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.stereotype.Service

@Service
class PushNotificationService(private val firebaseApp: FirebaseApp) {

    companion object : KLogging()

    fun send(
        fcmToken: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        val message = Message.builder()
            .setToken(fcmToken)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .putAllData(data)
            .build()

        try {
            FirebaseMessaging.getInstance(firebaseApp).send(message)
            logger.info { "Push notification sent: title='$title'" }
        } catch (e: FirebaseMessagingException) {
            logger.error { "Failed to send push notification to token $fcmToken: ${e.message}" }
        }
    }
}
