package org.example.service

import com.fandomatch.media.MediaService
import com.fandomatch.messaging.model.*
import com.fandomatch.notifications.PushNotificationService
import org.example.client.CoreAdapter
import org.example.client.UsersAdapter
import org.example.exceptions.CannotChatWithSelfException
import org.example.exceptions.ChatNotFoundException
import org.example.exceptions.NoMatchException
import org.example.models.db_models.Chat
import org.example.models.db_models.Message
import org.example.repository.ChatRepository
import org.example.repository.MediaItemRepository
import org.example.repository.MessageRepository
import org.example.repository.MessagingUserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ChatsService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val messagingUserRepository: MessagingUserRepository,
    private val mediaItemRepository: MediaItemRepository,
    private val usersAdapter: UsersAdapter,
    private val coreAdapter: CoreAdapter,
    private val mediaService: MediaService,
    private val notificationService: NotificationService,
    private val pushNotificationService: PushNotificationService
) {

    @Transactional(readOnly = true)
    fun getChatPreviews(userId: UUID, beforeTimestamp: Long?, size: Int): ChatPreviewsResponse {
        val chats = chatRepository.findAllByUserId(userId)

        val previews = chats.mapNotNull { chat ->
            val lastMessage = messageRepository.findTopByChatIdOrderByTimestampDesc(chat.id!!)
                ?: return@mapNotNull null

            if (beforeTimestamp != null && lastMessage.timestamp >= beforeTimestamp) {
                return@mapNotNull null
            }

            val participantId = if (chat.userId1 == userId) chat.userId2 else chat.userId1
            val participantName = resolveDisplayName(participantId)
            val newMessagesCount = messageRepository.countByChatIdAndSenderIdNotAndIsReadFalse(chat.id, userId)

            ChatPreview(
                userId = participantId.toString(),
                participantName = participantName,
                lastMessage = lastMessage.content,
                isLastMessageFromThisUser = lastMessage.senderId == userId,
                lastMessageTimestamp = lastMessage.timestamp,
                newMessagesCount = newMessagesCount
            )
        }.sortedByDescending { it.lastMessageTimestamp }.take(size)

        return ChatPreviewsResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = ChatPreviewsData(previews)
        )
    }

    @Transactional
    fun getOrCreateChat(currentUserId: UUID, targetUserId: UUID): ChatResponse {
        if (currentUserId == targetUserId) throw CannotChatWithSelfException()

        val chat = findOrCreateChat(currentUserId, targetUserId)
        val participantName = resolveDisplayName(targetUserId)

        return ChatResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = Chat(
                chatId = chat.id.toString(),
                participantId = targetUserId.toString(),
                participantName = participantName
            )
        )
    }

    @Transactional
    fun getMessages(currentUserId: UUID, targetUserId: UUID, beforeTimestamp: Long?, size: Int): ChatMessagesResponse {
        val chat = chatRepository.findByParticipants(currentUserId, targetUserId)
            .orElseThrow { ChatNotFoundException("$currentUserId-$targetUserId") }

        val messages = messageRepository.findByChatIdCursor(
            chatId = chat.id!!,
            beforeTimestamp = beforeTimestamp,
            pageable = PageRequest.of(0, size)
        )

        messageRepository.markAsRead(chat.id, currentUserId)

        val allMediaIds = messages.flatMap { it.mediaIds.toList() }
        val mediaTypeMap = if (allMediaIds.isNotEmpty()) {
            mediaItemRepository.findAllByMediaIdIn(allMediaIds).associate { it.mediaId to it.mediaType }
        } else {
            emptyMap()
        }

        return ChatMessagesResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = ChatMessagesData(messages.map { it.toDto(currentUserId, mediaTypeMap) })
        )
    }

    @Transactional
    fun sendMessage(currentUserId: UUID, targetUserId: UUID, request: SendMessageRequest): SendMessageResponse {
        if (currentUserId == targetUserId) throw CannotChatWithSelfException()

        val chat = findOrCreateChat(currentUserId, targetUserId)

        val message = messageRepository.save(
            Message(
                chatId = chat.id!!,
                senderId = currentUserId,
                content = request.content,
                mediaIds = request.mediaIds?.toTypedArray() ?: emptyArray(),
                timestamp = request.timestamp * 1000
            )
        )

        val mediaTypeMap = if (message.mediaIds.isNotEmpty()) {
            mediaItemRepository.findAllByMediaIdIn(message.mediaIds.toList()).associate { it.mediaId to it.mediaType }
        } else {
            emptyMap()
        }

        notificationService.pushMessage(targetUserId, currentUserId, message.toDto(targetUserId, mediaTypeMap))
        notificationService.pushMessage(currentUserId, targetUserId, message.toDto(currentUserId, mediaTypeMap))

        usersAdapter.getFcmToken(targetUserId)?.let { fcmToken ->
            pushNotificationService.sendDataMessage(fcmToken, mapOf(
                "type" to "chat",
                "chatId" to chat.id.toString(),
                "name" to resolveDisplayName(currentUserId)
            ))
        }

        val senderName = resolveDisplayName(currentUserId)
        val recipientName = resolveDisplayName(targetUserId)
        val recipientUnreadCount = messageRepository.countByChatIdAndSenderIdNotAndIsReadFalse(chat.id, targetUserId)

        notificationService.pushChatPreviewUpdate(
            currentUserId,
            ChatPreview(
                userId = targetUserId.toString(),
                participantName = recipientName,
                lastMessage = message.content,
                isLastMessageFromThisUser = true,
                lastMessageTimestamp = message.timestamp,
                newMessagesCount = 0
            )
        )
        notificationService.pushChatPreviewUpdate(
            targetUserId,
            ChatPreview(
                userId = currentUserId.toString(),
                participantName = senderName,
                lastMessage = message.content,
                isLastMessageFromThisUser = false,
                lastMessageTimestamp = message.timestamp,
                newMessagesCount = recipientUnreadCount
            )
        )

        return SendMessageResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = SendMessageData(messageId = message.id.toString())
        )
    }

    private fun findOrCreateChat(userId1: UUID, userId2: UUID): Chat {
        val (first, second) = if (userId1 < userId2) userId1 to userId2 else userId2 to userId1
        return chatRepository.findByParticipants(first, second).orElseGet {
            if (!coreAdapter.matchExists(userId1, userId2)) throw NoMatchException(userId1, userId2)
            chatRepository.save(Chat(userId1 = first, userId2 = second))
        }
    }

    private fun resolveDisplayName(userId: UUID): String {
        val cached = messagingUserRepository.findById(userId).orElse(null)
        if (cached != null) return cached.name ?: cached.username
        return usersAdapter.getUsernameById(userId) ?: userId.toString()
    }

    private fun Message.toDto(currentUserId: UUID, mediaTypeMap: Map<String, String>): com.fandomatch.messaging.model.Message {
        val mediaItems = mediaIds.mapNotNull { id ->
            val typeStr = mediaTypeMap[id] ?: return@mapNotNull null
            val mediaType = MediaType.entries.firstOrNull { it.value == typeStr } ?: return@mapNotNull null
            MediaItem(
                mediaId = id,
                mediaType = mediaType,
                url = mediaService.generateSignedDownloadUrl(id)
            )
        }.ifEmpty { null }

        return Message(
            messageId = id.toString(),
            isFromThisUser = senderId == currentUserId,
            content = content,
            timestamp = timestamp,
            mediaItems = mediaItems
        )
    }
}
