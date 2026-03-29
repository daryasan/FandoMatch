package org.example.stream.out

import com.fandomatch.core.model.EventType
import com.fandomatch.core.model.UserChangedEvent
import io.github.oshai.kotlinlogging.KLogging
import org.example.model.db_models.User
import org.example.utils.toChangedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class UserEventsSender(
    private val kafkaTemplate: KafkaTemplate<String, UserChangedEvent>
) {

    companion object : KLogging() {
        const val TOPIC_NAME = "user-changed-events"
    }

    fun sendUserCreatedEvent(user: User, eventType: EventType) {
        val event = user.toChangedEvent(eventType)
        kafkaTemplate.send(TOPIC_NAME, event.uid, event)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info(
                        "UserCreatedEvent sent successfully: userId={}, offset={}, partition={}",
                        event.uid, result.recordMetadata.offset(), result.recordMetadata.partition()
                    )
                } else {
                    logger.error("Failed to send UserCreatedEvent for userId={}", event.uid, ex)
                }
            }
    }
}