package org.example.service

import org.example.model.db_models.User
import org.example.model.db_models.UserCredentials
import org.example.repository.UserCredentialsRepository
import org.springframework.stereotype.Service

@Service
class UserCredentialsService(
    private val credentialsRepository: UserCredentialsRepository,
) {

    fun createCredentials(user: User, hashedPassword: String): UserCredentials {
        val credentials = UserCredentials(
            user = user,
            credentialType = "password",
            hash = hashedPassword
        )

        return credentialsRepository.save(credentials)
    }
}
