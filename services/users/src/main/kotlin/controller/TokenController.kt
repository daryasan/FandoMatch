package org.example.controller

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.model.PublicJwtResponse
import com.fandomatch.users.model.RefreshTokenResponse
import com.fandomatch.users.model.ResponseStatus
import org.springframework.http.ResponseEntity

class TokenController : TokenApi {

    override fun tokenPublicJwtGet(): ResponseEntity<PublicJwtResponse> {
        return ResponseEntity.ok(PublicJwtResponse("public key"))
    }

    override fun tokenRefreshTokenPost(): ResponseEntity<RefreshTokenResponse> {
        return ResponseEntity.ok(RefreshTokenResponse(status = ResponseStatus.SUCCESS))
    }
}