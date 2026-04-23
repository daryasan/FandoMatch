package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.example.exception.EmailAlreadyExistsException
import org.example.exception.InvalidUserInputData
import org.example.exception.UserNotFoundException
import org.example.exception.UsernameAlreadyExistsException
import org.example.repository.UserRepository
import org.example.service.TokenService
import org.example.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DataIntegrityViolationException
import utils.Constants.EMAIL
import utils.Constants.USERNAME
import utils.createUser

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var tokenService: TokenService

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `createUser should save user and return saved instance`() {
        val user = createUser(EMAIL, USERNAME)
        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns null
        every { userRepository.save(any()) } returns user

        val result = userService.createUser(EMAIL, USERNAME)

        verify(exactly = 1) { userRepository.save(any()) }
        assertEquals(EMAIL, result.email)
        assertEquals(USERNAME, result.username)
    }

    @Test
    fun `createUser should throw UsernameAlreadyExistsException when username already exists`() {
        val existingUser = createUser(EMAIL, USERNAME)
        every { userRepository.findByUsername(USERNAME) } returns existingUser

        assertThrows<UsernameAlreadyExistsException> {
            userService.createUser(EMAIL, USERNAME)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `createUser should throw EmailAlreadyExistsException when email already exists`() {
        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns createUser(EMAIL, "otheruser")

        assertThrows<EmailAlreadyExistsException> {
            userService.createUser(EMAIL, USERNAME)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `createUser should throw UsernameAlreadyExistsException as fallback on DataIntegrityViolationException`() {
        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns null
        every { userRepository.save(any()) } throws DataIntegrityViolationException("duplicate")

        assertThrows<UsernameAlreadyExistsException> {
            userService.createUser(EMAIL, USERNAME)
        }

        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `findByUsername should return user when found`() {
        val user = createUser(EMAIL, USERNAME)
        every { userRepository.findByUsername(USERNAME) } returns user

        val result = userService.findByUsername(USERNAME)

        assertEquals(user, result)
        verify(exactly = 1) { userRepository.findByUsername(USERNAME) }
    }

    @Test
    fun `findByUsername should throw UserNotFoundException when user does not exist`() {
        every { userRepository.findByUsername("ghost") } returns null

        assertThrows<UserNotFoundException> {
            userService.findByUsername("ghost")
        }

        verify(exactly = 1) { userRepository.findByUsername("ghost") }
    }

    @Test
    fun `findByEmail should return user when found`() {
        val user = createUser(EMAIL, USERNAME)
        every { userRepository.findByEmail(EMAIL) } returns user

        val result = userService.findUser(username = null)

        assertEquals(user, result)
        verify(exactly = 1) { userRepository.findByEmail(EMAIL) }
    }

    @Test
    fun `findUser should throw InvalidUserInputData when all credentials are null`() {
        assertThrows<InvalidUserInputData> {
            userService.findUser(null)
        }
    }

    @Test
    fun `findUser should try username first then email`() {
        val user = createUser(EMAIL, USERNAME)

        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns user

        val result = userService.findUser(USERNAME)

        assertEquals(user, result)

        verify(exactly = 1) { userRepository.findByUsername(USERNAME) }
        verify(exactly = 1) { userRepository.findByEmail(EMAIL) }
    }

    @Test
    fun `findUser should throw UserNotFoundException when no match found`() {
        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns null

        assertThrows<UserNotFoundException> {
            userService.findUser(USERNAME)
        }
    }
}
