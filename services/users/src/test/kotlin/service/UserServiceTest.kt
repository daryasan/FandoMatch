package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.example.exception.InvalidUserInputData
import org.example.exception.UserNotFoundException
import org.example.exception.UsernameAlreadyExistsException
import org.example.repository.UserRepository
import org.example.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DataIntegrityViolationException
import utils.Constants.EMAIL
import utils.Constants.PHONE
import utils.Constants.USERNAME
import utils.createUser

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `createUser should save user and return saved instance`() {
        val user = createUser(EMAIL, PHONE, USERNAME)
        every { userRepository.save(any()) } returns user

        val result = userService.createUser(EMAIL, PHONE, USERNAME)

        verify(exactly = 1) { userRepository.save(any()) }
        assertEquals(EMAIL, result.email)
        assertEquals(PHONE, result.phone)
        assertEquals(USERNAME, result.username)
    }

    @Test
    fun `createUser should throw InvalidUserInputData when email and phone are null`() {
        assertThrows<InvalidUserInputData> {
            userService.createUser(null, null, USERNAME)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `createUser should throw UsernameAlreadyExistsException when repository throws DataIntegrityViolationException`() {
        every { userRepository.save(any()) } throws DataIntegrityViolationException("duplicate")

        assertThrows<UsernameAlreadyExistsException> {
            userService.createUser(EMAIL, PHONE, USERNAME)
        }

        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `findByUsername should return user when found`() {
        val user = createUser(EMAIL, PHONE, USERNAME)
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
        val user = createUser(EMAIL, PHONE, USERNAME)
        every { userRepository.findByEmail(EMAIL) } returns user

        val result = userService.findUser(email = EMAIL, phone = null, username = null)

        assertEquals(user, result)
        verify(exactly = 1) { userRepository.findByEmail(EMAIL) }
    }

    @Test
    fun `findByEmail should skip email if not found and try phone`() {
        every { userRepository.findByEmail(EMAIL) } throws UserNotFoundException(EMAIL)
        every { userRepository.findByPhone(PHONE) } returns createUser(EMAIL, PHONE, USERNAME)

        val result = userService.findUser(email = EMAIL, phone = PHONE, username = null)

        assertEquals(PHONE, result.phone)
        verify(exactly = 1) { userRepository.findByEmail(EMAIL) }
        verify(exactly = 1) { userRepository.findByPhone(PHONE) }
    }

    @Test
    fun `findByPhone should return user when found`() {
        val user = createUser(EMAIL, PHONE, USERNAME)
        every { userRepository.findByPhone(PHONE) } returns user

        val result = userService.findUser(email = null, phone = PHONE, username = null)

        assertEquals(user, result)
        verify(exactly = 1) { userRepository.findByPhone(PHONE) }
    }

    @Test
    fun `findUser should throw InvalidUserInputData when all credentials are null`() {
        assertThrows<InvalidUserInputData> {
            userService.findUser(null, null, null)
        }
    }

    @Test
    fun `findUser should try username first then email then phone`() {
        val user = createUser(EMAIL, PHONE, USERNAME)

        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns null
        every { userRepository.findByPhone(PHONE) } returns user

        val result = userService.findUser(EMAIL, PHONE, USERNAME)

        assertEquals(user, result)

        verify(exactly = 1) { userRepository.findByUsername(USERNAME) }
        verify(exactly = 1) { userRepository.findByEmail(EMAIL) }
        verify(exactly = 1) { userRepository.findByPhone(PHONE) }
    }

    @Test
    fun `findUser should throw UserNotFoundException when no match found`() {
        every { userRepository.findByUsername(USERNAME) } returns null
        every { userRepository.findByEmail(EMAIL) } returns null
        every { userRepository.findByPhone(PHONE) } returns null

        assertThrows<UserNotFoundException> {
            userService.findUser(EMAIL, PHONE, USERNAME)
        }
    }
}
