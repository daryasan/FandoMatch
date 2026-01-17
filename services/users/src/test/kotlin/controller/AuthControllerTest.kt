package controller

import BaseTest
import com.fandomatch.users.model.ResponseStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import utils.userLoginRequest
import utils.userRegistrationRequest

@AutoConfigureMockMvc
class AuthControllerTest : BaseTest() {

    @Test
    fun `successful registration returns SUCCESS and jwt token`() {
        val request = userRegistrationRequest()

        val response = performRegisterRequestAndReturn(request)

        assertEquals(ResponseStatus.SUCCESS, response.status)
        assertNotNull(response.successResponse)
    }

    @Test
    fun `duplicate username returns ERROR with UsernameAlreadyExistsException`() {
        val request1 = userRegistrationRequest()
        val request2 = userRegistrationRequest(email = "another_email@mail.ru", phone = null)

        performRegisterRequestAndReturn(request1)
        // second time
        val response = performRegisterRequestAndReturn(request2)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals("USERNAME_ALREADY_EXISTS", response.errorResponse?.errorCode)
    }

    @Test
    fun `invalid input returns ERROR with InvalidUserInputData`() {
        val request = userRegistrationRequest(
            email = null,
            phone = null,
        )

        val response = performRegisterRequestAndReturn(request)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals("INVALID_USER_DATA", response.errorResponse?.errorCode)
    }

    @Test
    fun `successful login returns SUCCESS and jwt token`() {
        val registerRequest = userRegistrationRequest()
        performRegisterRequestAndReturn(registerRequest)

        val loginRequest = userLoginRequest()

        val response = performLoginRequestAndReturn(loginRequest)

        assertEquals(ResponseStatus.SUCCESS, response.status)
        assertNotNull(response.successResponse)
    }

    @Test
    fun `login with wrong password returns ERROR with UserCredentialMismatchException`() {
        val registerRequest = userRegistrationRequest(password = "qwerty")
        performRegisterRequestAndReturn(registerRequest)

        val loginRequest = userLoginRequest(password = "not_qwerty")

        val response = performLoginRequestAndReturn(loginRequest)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals("CREDENTIALS_MISMATCH", response.errorResponse?.errorCode)
    }
}
