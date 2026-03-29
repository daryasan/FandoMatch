package org.example.stream.`in`

import com.fandomatch.core.model.EventType
import com.fandomatch.core.model.UserChangedEvent
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.profile.ProfilesService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class UserEventConsumer(
    private val profilesService: ProfilesService
) {
    companion object : KLogging()

    @KafkaListener(topics = ["user-changed-events"], groupId = "core-service-group")
    fun handleUserChangedEvent(event: UserChangedEvent) {
        logger.info { "Received UserChangedEvent: uid=${event.uid}, type=${event.eventType}" }

        when (event.eventType) {
            EventType.CREATED ->  profilesService.createProfile(event)
            EventType.UPDATED -> profilesService.updateProfileCredentials(event)
        }
    }
}