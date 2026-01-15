package controller

import BaseTest
import com.fandomatch.users.model.ResponseStatus
import com.fandomatch.users.model.UserRegistrationRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

@AutoConfigureMockMvc
class AuthControllerTest : BaseTest() {

    @Test
    fun `successful registration returns SUCCESS and jwt token`() {
        val request = UserRegistrationRequest(
            email = "test@example.com",
            phone = "+123456789",
            username = "newuser",
            hashedPassword = "hashed_pass"
        )

        val response = performRegisterRequestAndReturn(request)

        assertEquals(ResponseStatus.SUCCESS, response.status)
        assertNotNull(response.successResponse)
    }

    @Test
    fun `duplicate username returns ERROR with UsernameAlreadyExistsException`() {
        val request = UserRegistrationRequest(
            email = "test@example.com",
            phone = "+123456789",
            username = "duplicateUser",
            hashedPassword = "hashed_pass"
        )

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        val response = performRegisterRequestAndReturn(request)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals("USERNAME_ALREADY_EXISTS", response.errorResponse?.errorCode)
    }

    @Test
    fun `invalid input returns ERROR with InvalidUserInputData`() {
        val request = UserRegistrationRequest(
            email = null,
            phone = null,
            username = "user123",
            hashedPassword = "hashed_pass"
        )

        val response = performRegisterRequestAndReturn(request)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals("INVALID_USER_DATA", response.errorResponse?.errorCode)
    }
}
