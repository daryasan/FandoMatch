package org.example.service

import org.example.models.User
import org.example.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(
        email: String?,
        phone: String?,
        username: String,
    ) : User {
        val userToSave = User(
            email = email,
            phone = phone,
            username = username,
        )

        userRepository.save(userToSave)
        return userToSave
    }

}