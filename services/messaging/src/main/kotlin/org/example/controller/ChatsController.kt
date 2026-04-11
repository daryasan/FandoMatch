package org.example.controller

import com.fandomatch.messaging.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.ChatsService
import org.example.service.TokenParserService
import org.example.util.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/messaging/chats")
class ChatsController(
    private val chatsService: ChatsService,
    private val tokenParserService: TokenParserService
) {

    companion object : KLogging()

    @PostMapping("/previews")
    fun getChatPreviews(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: ChatPreviewsRequest
    ): ResponseEntity<ChatPreviewsResponse> {
        val userId = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /messaging/chats/previews",
            metaUuid = userId.toString(),
            errorMapper = { getChatPreviewsErrorResponse(it) }
        ) {
            chatsService.getChatPreviews(userId, request.beforeTimestamp, request.propertySize)
        }
    }

    @GetMapping("/{user_id}")
    fun getChat(
        @RequestHeader("Authorization") token: String,
        @PathVariable("user_id") targetUserId: String
    ): ResponseEntity<ChatResponse> {
        val userId = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "GET /messaging/chats/$targetUserId",
            metaUuid = userId.toString(),
            errorMapper = { getChatErrorResponse(it) }
        ) {
            chatsService.getOrCreateChat(userId, UUID.fromString(targetUserId))
        }
    }

    @PostMapping("/{user_id}/messages")
    fun getMessages(
        @RequestHeader("Authorization") token: String,
        @PathVariable("user_id") targetUserId: String,
        @RequestBody request: ChatMessagesRequest
    ): ResponseEntity<ChatMessagesResponse> {
        val userId = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /messaging/chats/$targetUserId/messages",
            metaUuid = userId.toString(),
            errorMapper = { getChatMessagesErrorResponse(it) }
        ) {
            chatsService.getMessages(userId, UUID.fromString(targetUserId), request.beforeTimestamp, request.propertySize)
        }
    }

    @PostMapping("/{user_id}/send")
    fun sendMessage(
        @RequestHeader("Authorization") token: String,
        @PathVariable("user_id") targetUserId: String,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<SendMessageResponse> {
        val userId = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /messaging/chats/$targetUserId/send",
            metaUuid = userId.toString(),
            errorMapper = { getSendMessageErrorResponse(it) }
        ) {
            chatsService.sendMessage(userId, UUID.fromString(targetUserId), request)
        }
    }
}
