package org.example.stream.`in`

import com.fandomatch.core.model.UserChangedEvent
import io.github.oshai.kotlinlogging.KLogging
import org.example.models.db_models.MessagingUser
import org.example.repository.MessagingUserRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class UserEventConsumer(
    private val messagingUserRepository: MessagingUserRepository
) {
    companion object : KLogging()

    @KafkaListener(topics = ["user-changed-events"], groupId = "messaging-service-group")
    fun handleUserChangedEvent(event: UserChangedEvent) {
        logger.info { "Received UserChangedEvent: uid=${event.uid}, type=${event.eventType}" }
        messagingUserRepository.save(
            MessagingUser(
                userId = UUID.fromString(event.uid),
                username = event.username,
                name = event.name,
                avatarMediaId = event.avatarMediaId,
                updatedAt = Instant.now()
            )
        )
    }
}
