package controller

import com.fandomatch.core.model.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.controller.MatchController
import org.example.exceptions.AlreadyReactedException
import org.example.exceptions.UserNotFoundException
import org.example.models.UserTokenData
import org.example.service.MatchesService
import org.example.service.TokenParserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import utils.Constants.TARGET_USER_ID
import utils.Constants.USER_ID
import utils.Constants.USERNAME

@ExtendWith(MockKExtension::class)
class MatchControllerTest {

    @MockK
    lateinit var matchesService: MatchesService

    @MockK
    lateinit var tokenParserService: TokenParserService

    @InjectMockKs
    private lateinit var matchController: MatchController

    private val token = "Bearer test-token"
    private val tokenData = UserTokenData(userId = USER_ID, username = USERNAME)

    // --- getNextBatch ---

    @Test
    fun `getNextBatch should return SUCCESS with candidates`() {
        val request = MatchBatchRequest(batchSize = 5)
        val response = MatchCandidateBatchResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchCandidateBatchData(emptyList())
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { matchesService.getNextCandidates(USER_ID, 5) } returns response

        val result = matchController.getNextBatch(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.SUCCESS, result.body!!.status)
        verify(exactly = 1) { matchesService.getNextCandidates(USER_ID, 5) }
    }

    // --- react ---

    @Test
    fun `react should return SUCCESS with LIKED status`() {
        val request = MatchActionRequest(
            targetUuid = TARGET_USER_ID.toString(),
            action = MatchActionRequest.Action.LIKE
        )
        val response = MatchActionResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchActionResult(status = MatchActionResult.Status.LIKED)
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { matchesService.react(USER_ID, TARGET_USER_ID, "LIKE") } returns response

        val result = matchController.react(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.SUCCESS, result.body!!.status)
        assertEquals(MatchActionResult.Status.LIKED, result.body!!.successResponse!!.status)
    }

    @Test
    fun `react should return SUCCESS with MATCH status on mutual like`() {
        val request = MatchActionRequest(
            targetUuid = TARGET_USER_ID.toString(),
            action = MatchActionRequest.Action.LIKE
        )
        val response = MatchActionResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchActionResult(status = MatchActionResult.Status.MATCH)
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { matchesService.react(USER_ID, TARGET_USER_ID, "LIKE") } returns response

        val result = matchController.react(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(MatchActionResult.Status.MATCH, result.body!!.successResponse!!.status)
    }

    @Test
    fun `react should return ERROR when target user not found`() {
        val request = MatchActionRequest(
            targetUuid = TARGET_USER_ID.toString(),
            action = MatchActionRequest.Action.LIKE
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { matchesService.react(USER_ID, TARGET_USER_ID, "LIKE") } throws
                UserNotFoundException(TARGET_USER_ID.toString())

        val result = matchController.react(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.ERROR, result.body!!.status)
        assertEquals("USER_NOT_FOUND", result.body!!.errorResponse!!.errorCode)
    }

    @Test
    fun `react should return ERROR when already reacted`() {
        val request = MatchActionRequest(
            targetUuid = TARGET_USER_ID.toString(),
            action = MatchActionRequest.Action.LIKE
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { matchesService.react(USER_ID, TARGET_USER_ID, "LIKE") } throws
                AlreadyReactedException(TARGET_USER_ID.toString())

        val result = matchController.react(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.ERROR, result.body!!.status)
        assertEquals("ALREADY_REACTED", result.body!!.errorResponse!!.errorCode)
    }
}
