package org.example.controller

import com.fandomatch.users.model.*
import com.fandomatch.users.model.ResponseStatus
import org.example.exception.BusinessException
import org.example.service.TokenService
import org.example.utils.getRefreshTokenErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/token")
class TokenController(
    private val tokenService: TokenService
) {

    @GetMapping("/public/jwt")
    fun tokenPublicJwtGet(): ResponseEntity<PublicJwtResponse> {
        return ResponseEntity.ok(PublicJwtResponse(tokenService.getPublicKey()))
    }

    @PostMapping("/refresh")
    fun tokenRefreshPost(@RequestBody refreshToken: RefreshToken): ResponseEntity<RefreshTokenResponse> {
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
