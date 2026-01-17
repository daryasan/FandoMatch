package org.example.service

import org.example.exception.UserCredentialMismatchException
import org.example.exception.UserCredentialNotFoundException
import org.example.model.db_models.CredentialType
import org.example.model.db_models.User
import org.example.model.db_models.UserCredential
import org.example.repository.UserCredentialsRepository
import org.example.service.AuthService.Companion.logger
import org.example.service.validation.PasswordHasherService
import org.example.service.validation.SaltGenerator
import org.springframework.stereotype.Service

@Service
class UserCredentialsService(
    private val credentialsRepository: UserCredentialsRepository,
    private val passwordHasherService: PasswordHasherService,
    private val saltGenerator: SaltGenerator,
) {

    fun createCredentials(user: User, password: String): UserCredential {
        val passwordSalt = saltGenerator.generateSalt()
        val passwordHash = passwordHasherService.hash(password, passwordSalt)

        val credentials = UserCredential(
            user = user,
            credentialType = CredentialType.PASSWORD,
            hash = passwordHash,
            salt = passwordSalt
        )

        return credentialsRepository.save(credentials)
    }

    fun verifyCredential(
        user: User,
        credential: String,
        credentialType: CredentialType = CredentialType.PASSWORD
    ) {
        val passwordCredential = user.credentials.find { it.credentialType == credentialType }
            ?: throw UserCredentialNotFoundException(credentialType.name)

        if (!passwordHasherService.matches(credential, passwordCredential.hash!!, passwordCredential.salt!!)) {
            logger.error { "Login failed: password mismatch for user ${user.username}" }
            throw UserCredentialMismatchException(passwordCredential.credentialType.name)
        }

        logger.info { "Credential match successful for user ${user.username}" }
    }
}
