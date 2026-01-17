package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.example.exception.UserCredentialMismatchException
import org.example.exception.UserCredentialNotFoundException
import org.example.model.db_models.enums.CredentialType
import org.example.model.db_models.UserCredential
import org.example.repository.UserCredentialsRepository
import org.example.service.UserCredentialsService
import org.example.service.validation.PasswordHasherService
import org.example.service.validation.SaltGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import utils.Constants.EMAIL
import utils.Constants.PASSWORD
import utils.Constants.PASSWORD_SALT
import utils.Constants.PHONE
import utils.Constants.USERNAME
import utils.createUser
import java.util.*

@ExtendWith(MockKExtension::class)
class UserCredentialsServiceTest {

    @MockK
    lateinit var credentialsRepository: UserCredentialsRepository

    @MockK
    lateinit var passwordHasherService: PasswordHasherService

    @MockK
    lateinit var saltGenerator: SaltGenerator

    @InjectMockKs
    private lateinit var credentialsService: UserCredentialsService

    @Test
    fun `createCredentials should save credentials and return saved instance`() {
        // given
        val user = createUser(EMAIL, PHONE, USERNAME)
        val rawPassword = "front_hash_123"
        val backendHash = "argon2_hash"

        every { saltGenerator.generateSalt() } returns PASSWORD_SALT
        every { passwordHasherService.hash(rawPassword, PASSWORD_SALT) } returns backendHash

        val savedCredentials = UserCredential(
            id = UUID.randomUUID(),
            user = user,
            credentialType = CredentialType.PASSWORD,
            hash = backendHash,
            salt = PASSWORD_SALT
        )

        every { credentialsRepository.save(any()) } returns savedCredentials

        // when
        val result = credentialsService.createCredentials(user, rawPassword)

        // then
        verify(exactly = 1) { saltGenerator.generateSalt() }
        verify(exactly = 1) { passwordHasherService.hash(rawPassword, PASSWORD_SALT) }
        verify(exactly = 1) { credentialsRepository.save(any()) }

        assertEquals(savedCredentials.id, result.id)
        assertEquals(user, result.user)
        assertEquals(CredentialType.PASSWORD, result.credentialType)
        assertEquals(backendHash, result.hash)
        assertEquals(PASSWORD_SALT, result.salt)
    }

    @Test
    fun `verifyCredential should succeed when password matches`() {
        val user = createUser(EMAIL, PHONE, USERNAME)
        val storedHash = "argon2_hash"

        val credential = UserCredential(
            id = UUID.randomUUID(),
            user = user,
            credentialType = CredentialType.PASSWORD,
            hash = storedHash,
            salt = PASSWORD_SALT
        )

        user.credentials.add(credential)

        every { passwordHasherService.matches(PASSWORD, storedHash, PASSWORD_SALT) } returns true

        credentialsService.verifyCredential(user, PASSWORD)

        verify(exactly = 1) { passwordHasherService.matches(PASSWORD, storedHash, PASSWORD_SALT) }
    }

    @Test
    fun `verifyCredential should throw UserCredentialNotFoundException when no credential of given type exists`() {
        val user = createUser(EMAIL, PHONE, USERNAME)

        assertThrows<UserCredentialNotFoundException> {
            credentialsService.verifyCredential(user, PASSWORD)
        }
    }

    @Test
    fun `verifyCredential should throw UserCredentialMismatchException when password does not match`() {
        val user = createUser(EMAIL, PHONE, USERNAME)
        val storedHash = "argon2_hash"
        val wrongPassword = "wrongPassword"

        val credential = UserCredential(
            id = UUID.randomUUID(),
            user = user,
            credentialType = CredentialType.PASSWORD,
            hash = storedHash,
            salt = PASSWORD_SALT
        )

        user.credentials.add(credential)

        every { passwordHasherService.matches(wrongPassword, storedHash, PASSWORD_SALT) } returns false

        assertThrows<UserCredentialMismatchException> {
            credentialsService.verifyCredential(user, wrongPassword)
        }

        verify(exactly = 1) { passwordHasherService.matches(wrongPassword, storedHash, PASSWORD_SALT) }
    }
}
