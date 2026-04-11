package org.example.stream.`in`

import com.fandomatch.core.model.MatchEvent
import io.github.oshai.kotlinlogging.KLogging
import org.example.models.db_models.Chat
import org.example.repository.ChatRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MatchEventConsumer(
    private val chatRepository: ChatRepository
) {
    companion object : KLogging()

    @KafkaListener(topics = ["match-events"], groupId = "messaging-service-group")
    fun handleMatchEvent(event: MatchEvent) {
        logger.info { "Received MatchEvent: matchId=${event.matchId}" }
        val u1 = UUID.fromString(event.userId1)
        val u2 = UUID.fromString(event.userId2)
        val (first, second) = if (u1 < u2) u1 to u2 else u2 to u1

        if (!chatRepository.findByParticipants(first, second).isPresent) {
            chatRepository.save(Chat(userId1 = first, userId2 = second))
            logger.info { "Chat created for match ${event.matchId}" }
        }
    }
}
