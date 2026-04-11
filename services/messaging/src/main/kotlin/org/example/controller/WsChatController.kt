package org.example.controller

import com.fandomatch.messaging.model.SendMessageRequest
import org.example.service.ChatsService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import java.security.Principal
import java.util.UUID

@Controller
class WsChatController(
    private val chatsService: ChatsService
) {

    @MessageMapping("/chats/{targetUserId}/send")
    fun sendMessage(
        @DestinationVariable targetUserId: String,
        @Payload request: SendMessageRequest,
        principal: Principal
    ) {
        val currentUserId = UUID.fromString(principal.name)
        chatsService.sendMessage(currentUserId, UUID.fromString(targetUserId), request)
    }
}
