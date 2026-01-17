package org.example.controller

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.model.*
import org.example.exception.BusinessException
import org.example.service.TokenService
import org.example.utils.getRefreshTokenErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class TokenController(
    private val tokenService: TokenService
) : TokenApi {

    override fun tokenPublicJwtGet(): ResponseEntity<PublicJwtResponse> {
        return ResponseEntity.ok(PublicJwtResponse("public key"))
    }

    override fun tokenRefreshPost(refreshToken: RefreshToken): ResponseEntity<RefreshTokenResponse> {
        val tokens = try {
            tokenService.refreshAccessToken(refreshToken.refreshToken)
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getRefreshTokenErrorResponse(e))
        }

        return ResponseEntity.ok(
            RefreshTokenResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = RefreshAndAccessTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken
                )
            )
        )
    }

}