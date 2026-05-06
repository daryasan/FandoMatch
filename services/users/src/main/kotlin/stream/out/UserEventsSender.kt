package org.example.stream.out

import com.fandomatch.core.model.EventType
import com.fandomatch.core.model.UserChangedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.model.db_models.User
import org.example.utils.toChangedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class UserEventsSender(
    private val kafkaTemplate: KafkaTemplate<String, UserChangedEvent>
) {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val TOPIC_NAME = "user-changed-events"
    }

    fun sendUserCreatedEvent(user: User, eventType: EventType, name: String, birthDate: Long) {
        val event = user.toChangedEvent(eventType, name, birthDate)
        sendEvent(event)
    }

    fun sendUserEvent(user: User, eventType: EventType) {
        val event = user.toChangedEvent(eventType)
        sendEvent(event)
    }

    private fun sendEvent(event: UserChangedEvent) {
        kafkaTemplate.send(TOPIC_NAME, event.uid, event)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info(
                        "UserEvent sent successfully: userId={}, type={}, offset={}, partition={}",
                        event.uid, event.eventType, result.recordMetadata.offset(), result.recordMetadata.partition()
                    )
                } else {
                    logger.error("Failed to send UserEvent for userId={}", event.uid, ex)
                }
            }
    }
}
