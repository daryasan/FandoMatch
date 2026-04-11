package org.example.service

import io.github.oshai.kotlinlogging.KLogging
import org.example.exception.EmailAlreadyExistsException
import org.example.exception.InvalidUserInputData
import org.example.exception.UserNotFoundException
import org.example.exception.UsernameAlreadyExistsException
import org.example.model.db_models.User
import org.example.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
) {

    companion object : KLogging()

    fun createUser(
        email: String,
        username: String,
    ): User {
        userRepository.findByUsername(username)?.let {
            throw UsernameAlreadyExistsException(username)
        }
        userRepository.findByEmail(email)?.let {
            throw EmailAlreadyExistsException(email)
        }

        val userToSave = User(
            email = email,
            username = username,
        )
        try {
            userRepository.save(userToSave)
            logger.info { "Created user with uid: ${userToSave.uid}, username: $username" }
            return userToSave
        } catch (e: DataIntegrityViolationException) {
            logger.error { "Error creating user: duplicate data for username=$username, email=$email" }
            throw UsernameAlreadyExistsException(username)
        }
    }

    fun findUserByToken(accessToken: String): User {
        val uuid = tokenService.getUserIdByToken(accessToken)
        return userRepository.findById(uuid).orElseThrow { throw UserNotFoundException("id") }
    }

    fun findUserByUuid(uuid: UUID): User {
        return userRepository.findById(uuid).orElseThrow { throw UserNotFoundException("id") }
    }

    fun findUser(
        email: String?,
        username: String?,
    ): User {
        if (email == null && username == null)
            throw InvalidUserInputData("Cannot find user when all credentials are null")
        username?.let { runCatching { return findByUsername(it) } }
        email?.let { runCatching { return findByEmail(it) } }
        throw UserNotFoundException("User not found by provided credentials")
    }

    fun findByUsername(username: String): User {
        val user = userRepository.findByUsername(username)
        return user ?: throw UserNotFoundException(username)
    }

    private fun findByEmail(email: String): User {
        val user = userRepository.findByEmail(email)
        return user ?: throw UserNotFoundException(email)
    }
}
