package org.example.service

import io.github.oshai.kotlinlogging.KLogging
import org.example.service.security.JwtService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val tokenService: TokenService,
) {

    companion object : KLogging()

    fun register(
        email: String?,
        phone: String?,
        username: String,
    ): String {
        logger.info { "Got request for user registration with username=$username" }

        val savedUser = userService.createUser(
            email = email,
            phone = phone,
            username = username,
        )

        val token = jwtService.generateToken(savedUser)
        tokenService.saveAccessToken(savedUser, token)
        return token.token
    }

}