package org.example.service

import com.fandomatch.messaging.model.ChatPreview
import com.fandomatch.messaging.model.Message
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun pushMessage(recipientId: UUID, senderId: UUID, message: Message) {
        messagingTemplate.convertAndSendToUser(
            recipientId.toString(),
            "/queue/chats/${senderId}/messages",
            message
        )
    }

    fun pushChatPreviewUpdate(userId: UUID, preview: ChatPreview) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/chat-previews",
            preview
        )
    }
}
