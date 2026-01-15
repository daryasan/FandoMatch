package service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.example.model.db_models.UserCredentials
import org.example.repository.UserCredentialsRepository
import org.example.service.UserCredentialsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.createUser
import java.util.*

@ExtendWith(MockKExtension::class)
class UserCredentialsServiceTest {

    @MockK
    lateinit var credentialsRepository: UserCredentialsRepository

    private lateinit var credentialsService: UserCredentialsService

    @BeforeEach
    fun setup() {
        credentialsService = UserCredentialsService(credentialsRepository)
    }

    @Test
    fun `createCredentials should save credentials and return saved instance`() {
        // given
        val user = createUser("test@example.com", "+123456789", "testuser")
        val hashedPassword = "hashed_password_123"

        val savedCredentials = UserCredentials(
            id = UUID.randomUUID(),
            user = user,
            credentialType = "password",
            hash = hashedPassword
        )

        every { credentialsRepository.save(any()) } returns savedCredentials

        // when
        val result = credentialsService.createCredentials(user, hashedPassword)

        // then
        verify(exactly = 1) { credentialsRepository.save(any()) }

        assertEquals(savedCredentials.id, result.id)
        assertEquals(user, result.user)
        assertEquals("password", result.credentialType)
        assertEquals(hashedPassword, result.hash)
    }
}
