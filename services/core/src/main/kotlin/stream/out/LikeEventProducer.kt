package org.example.stream.out

import com.fandomatch.core.model.LikeEvent
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LikeEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    companion object : KLogging() {
        const val TOPIC = "like-events"
    }

    fun send(likerId: UUID, likedUserId: UUID) {
        val event = LikeEvent(
            likerId = likerId.toString(),
            likedUserId = likedUserId.toString()
        )
        kafkaTemplate.send(TOPIC, likerId.toString(), event)
        logger.info { "Sent LikeEvent to $TOPIC: likerId=$likerId, likedUserId=$likedUserId" }
    }
}
