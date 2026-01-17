package controller

import BaseTest
import com.fandomatch.users.model.ResponseStatus
import org.example.exception.ErrorCode
import org.example.model.db_models.enums.TokenType
import org.example.repository.TokenRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import utils.userRegistrationRequest
import java.time.LocalDateTime

@AutoConfigureMockMvc
class TokenControllerTest : BaseTest() {

    @Autowired
    lateinit var tokenRepository: TokenRepository

    @Test
    fun `happy path - refresh returns new access token`() {
        val register = userRegistrationRequest()
        val regResponse = performRegisterRequestAndReturn(register)

        val refreshToken = regResponse.successResponse!!.refreshToken

        val response = performTokenRefreshRequestAndReturn(refreshToken)

        assertEquals(ResponseStatus.SUCCESS, response.status)
        assertNotNull(response.successResponse?.accessToken)
        assertEquals(refreshToken, response.successResponse?.refreshToken)
    }

    @Test
    fun `expired refresh token returns ERROR`() {
        val register = userRegistrationRequest()
        val regResponse = performRegisterRequestAndReturn(register)

        val refreshToken = regResponse.successResponse!!.refreshToken

        val entity = tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)!!
        entity.expiresAt = LocalDateTime.now().minusDays(1)
        tokenRepository.save(entity)

        val response = performTokenRefreshRequestAndReturn(refreshToken)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals(ErrorCode.REFRESH_TOKEN_INVALID.name, response.errorResponse?.errorCode)
    }

    @Test
    fun `revoked refresh token returns ERROR`() {
        val register = userRegistrationRequest()
        val regResponse = performRegisterRequestAndReturn(register)

        val refreshToken = regResponse.successResponse!!.refreshToken

        val entity = tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)!!
        entity.revoked = true
        tokenRepository.save(entity)

        val response = performTokenRefreshRequestAndReturn(refreshToken)

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals(ErrorCode.REFRESH_TOKEN_INVALID.name, response.errorResponse?.errorCode)
    }

    @Test
    fun `refresh token not found returns ERROR`() {
        val response = performTokenRefreshRequestAndReturn("non-existing-refresh-token")

        assertEquals(ResponseStatus.ERROR, response.status)
        assertEquals(ErrorCode.REFRESH_TOKEN_INVALID.name, response.errorResponse?.errorCode)
    }
}
