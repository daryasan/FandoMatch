package org.example.config

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.exceptions.UsersNotRespondingException
import org.example.service.TokenParserService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilterConfig(
    private val tokenParserService: TokenParserService
) : OncePerRequestFilter() {

    override fun shouldNotFilterErrorDispatch() = true

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        logger.info { "Start authorizing user" }
        val header = request.getHeader("Authorization")

        if (header.isNullOrBlank() || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val auth = UsernamePasswordAuthenticationToken(
                tokenParserService.parse(header),
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )
            SecurityContextHolder.getContext().authentication = auth
        } catch (e: UsersNotRespondingException) {
            logger.error { "Users service unavailable during JWT parsing: ${e.message}" }
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Auth service unavailable")
            return
        } catch (e: JwtException) {
            logger.warn { "Invalid JWT: ${e.message}" }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token")
            return
        } catch (e: IllegalArgumentException) {
            logger.warn { "Malformed JWT claims: ${e.message}" }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token")
            return
        } catch (e: Exception) {
            logger.error { "Unexpected error during token validation: ${e.message}" }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed")
            return
        }
        filterChain.doFilter(request, response)
    }
}


