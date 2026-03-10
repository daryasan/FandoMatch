package org.example.service

import io.jsonwebtoken.JwtParser
import org.example.models.UserTokenData
import java.util.*

class TokenParserService(
    private val jwtParser: JwtParser
) {

    fun parse(unparsedToken: String): UserTokenData {
        val claims = jwtParser
            .parseSignedClaims(unparsedToken)
            .payload

        val userId = UUID.fromString(claims.subject)
        val username = claims["username"] as String

        return UserTokenData(
            userId = userId,
            username = username
        )
    }
}