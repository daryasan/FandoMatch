package org.example.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            logger.warn { "Invalid JWT: ${e.message}" }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired token")
        }
    }
}


