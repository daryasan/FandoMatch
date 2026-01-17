package org.example.service

import io.github.oshai.kotlinlogging.KLogging
import org.example.service.validation.UserValidator
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val userCredentialsService: UserCredentialsService,
    private val userValidator: UserValidator
) {

    companion object : KLogging()

    fun register(
        email: String?,
        phone: String?,
        username: String,
        password: String
    ): String {
        logger.info { "Got request for user registration with username=$username" }

        val savedUser = userService.createUser(
            email = email,
            phone = phone,
            username = username,
        )

        userCredentialsService.createCredentials(savedUser, password)

        return tokenService.generateAndSaveToken(savedUser)
    }

    fun login(
        email: String?,
        phone: String?,
        username: String?,
        password: String
    ): String {
        logger.info { "Got request for user login with username=$username" }

        val foundUser = userService.findUser(email, phone, username)
        userValidator.validateUserBeforeLogin(foundUser)

        userCredentialsService.verifyCredential(foundUser, password)
        return tokenService.generateAndSaveToken(foundUser)
    }


}