package service

import BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.example.exception.InvalidUserInputData
import org.example.exception.UsernameAlreadyExistsException
import org.example.repository.UserRepository
import org.example.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import utils.createUser

class UserServiceTest : BaseTest() {

    @MockK
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `createUser should save user and return saved instance`() {
        // given
        val email = "test@example.com"
        val phone = "+123456789"
        val username = "testuser"
        val user = createUser(email, phone, username)

        every { userRepository.save(any()) } returns user

        // when
        val result = userService.createUser(email, phone, username)

        // then
        verify(exactly = 1) { userRepository.save(any()) }
        assertEquals(email, result.email)
        assertEquals(phone, result.phone)
        assertEquals(username, result.username)
    }

    @Test
    fun `createUser should throw InvalidUserInputData when email and phone are null`() {
        // when
        assertThrows<InvalidUserInputData> {
            userService.createUser(null, null, "user123")
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `createUser should throw UsernameAlreadyExistsException when repository throws DataIntegrityViolationException`() {
        // given
        val email = "duplicate@example.com"
        val phone = "+111111111"
        val username = "duplicateUser"

        every { userRepository.save(any()) } throws DataIntegrityViolationException("duplicate")

        // when
        assertThrows<UsernameAlreadyExistsException> {
            userService.createUser(email, phone, username)
        }

        verify(exactly = 1) { userRepository.save(any()) }
    }
}
