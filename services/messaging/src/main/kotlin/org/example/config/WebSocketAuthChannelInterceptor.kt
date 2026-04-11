package org.example.config

import org.example.service.TokenParserService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class WebSocketAuthChannelInterceptor(
    private val tokenParserService: TokenParserService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")
            if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ")) {
                val tokenData = tokenParserService.parse(authHeader)
                val auth = UsernamePasswordAuthenticationToken(
                    tokenData.userId.toString(),
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                accessor.user = auth
            }
        }

        return message
    }
}
