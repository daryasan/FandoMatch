package org.example.stream.out

import com.fandomatch.core.model.MatchEvent
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MatchEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    companion object : KLogging() {
        const val TOPIC = "match-events"
    }

    fun sendMatchEvent(matchId: UUID, userId1: UUID, userId2: UUID) {
        val event = MatchEvent(
            matchId = matchId.toString(),
            userId1 = userId1.toString(),
            userId2 = userId2.toString()
        )
        kafkaTemplate.send(TOPIC, matchId.toString(), event)
        logger.info { "Sent MatchEvent to $TOPIC: matchId=$matchId, user1=$userId1, user2=$userId2" }
    }
}