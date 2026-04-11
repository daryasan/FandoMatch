package org.example.service

import com.fandomatch.core.model.EventType
import io.github.oshai.kotlinlogging.KLogging
import jakarta.transaction.Transactional
import org.example.model.AuthTokens
import org.example.service.validation.UserValidator
import org.example.stream.out.UserEventsSender
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val userCredentialsService: UserCredentialsService,
    private val userValidator: UserValidator,
    private val userEventsSender: UserEventsSender,
) {

    companion object : KLogging()

    fun register(
        email: String,
        username: String,
        password: String,
        birthDate: Long,
        name: String
    ): AuthTokens {
        logger.info { "Got request for user registration with username=$username" }

        val savedUser = userService.createUser(
            email = email,
            username = username,
        )

        userCredentialsService.createCredentials(savedUser, password)
        userEventsSender.sendUserCreatedEvent(savedUser, EventType.CREATED, name, birthDate)

        return tokenService.generateAndSaveTokens(savedUser)
    }

    @Transactional
    fun login(
        email: String?,
        username: String?,
        password: String
    ): AuthTokens {
        logger.info { "Got request for user login with username=$username" }

        val foundUser = userService.findUser(email, username)
        userValidator.validateUserBeforeLogin(foundUser)

        userCredentialsService.verifyCredential(foundUser, password)
        return tokenService.generateAndSaveTokens(foundUser)
    }

    fun changePassword(accessToken: String, oldPassword: String, newPassword: String) {
        val user = userService.findUserByToken(accessToken)
        userCredentialsService.changePassword(user, oldPassword, newPassword)
    }
}
