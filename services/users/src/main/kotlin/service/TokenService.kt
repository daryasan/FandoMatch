package org.example.service

import io.github.oshai.kotlinlogging.KLogging
import org.example.model.GeneratedToken
import org.example.model.db_models.Token
import org.example.model.db_models.User
import org.example.repository.TokenRepository
import org.example.service.security.JwtService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TokenService(
    private val tokenRepository: TokenRepository,
    private val jwtService: JwtService,
) {

    companion object : KLogging()

    fun saveAccessToken(user: User, token: GeneratedToken) {
        val now = LocalDateTime.now()
        val tokenToSave = Token(
            user = user,
            tokenHash = jwtService.getTokenHash(token.token),
            issuedAt = now,
            expiresAt = token.expiresAt,
            revoked = false
        )
        tokenRepository.save(tokenToSave)
        logger.info { "Saved access token for user ${user.username}" }
    }

}