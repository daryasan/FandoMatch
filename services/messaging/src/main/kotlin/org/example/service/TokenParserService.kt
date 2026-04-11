package org.example.service

import io.jsonwebtoken.JwtParser
import org.example.models.UserTokenData
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenParserService(
    private val jwtParser: JwtParser
) {
    companion object {
        const val TOKEN_PREFIX = "Bearer "
    }

    fun parse(unparsedToken: String): UserTokenData {
        val tokenWithoutPrefix = unparsedToken.removePrefix(TOKEN_PREFIX)
        val claims = jwtParser
            .parseSignedClaims(tokenWithoutPrefix)
            .payload

        val userId = UUID.fromString(claims.subject)
        val username = claims["username"] as String

        return UserTokenData(userId = userId, username = username)
    }
}
