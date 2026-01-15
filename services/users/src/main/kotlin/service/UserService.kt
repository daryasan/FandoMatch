package org.example.service

import io.github.oshai.kotlinlogging.KLogging
import org.example.exception.InvalidUserInputData
import org.example.exception.UsernameAlreadyExistsException
import org.example.model.db_models.User
import org.example.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    companion object : KLogging()

    fun createUser(
        email: String?,
        phone: String?,
        username: String,
    ): User {
        if (email == null && phone == null) throw InvalidUserInputData("Either phone or email must be nit null")
        val userToSave = User(
            email = email,
            phone = phone,
            username = username,
        )
        try {
            userRepository.save(userToSave)
            logger.info { "Created user with uid: ${userToSave.internalId}, username: $username" }
            return userToSave
        } catch (e: DataIntegrityViolationException) {
            logger.error { "Error creating user: username $username already exists" }
            throw UsernameAlreadyExistsException(username)
        }
    }
}