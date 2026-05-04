package org.example.config.filters

import io.github.oshai.kotlinlogging.KLogging
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.exception.BusinessException
import org.example.service.security.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    companion object : KLogging()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val header = request.getHeader("Authorization") ?: return filterChain.doFilter(request, response)

        if (!header.startsWith("Bearer ")) {
            return filterChain.doFilter(request, response)
        }

        val token = header.removePrefix("Bearer ").trim()

        try {
            val userDetails = jwtService.validateAndLoadUser(token)
            val auth = UsernamePasswordAuthenticationToken(userDetails, null, listOf(SimpleGrantedAuthority("ROLE_USER")))
            SecurityContextHolder.getContext().authentication = auth
        } catch (e: BusinessException) {
            logger.warn { "Invalid JWT: ${e.message}" }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired token")
            return
        } catch (e: JwtException) {
            logger.warn { "Invalid JWT: ${e.message}" }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired token")
            return
        } catch (e: IllegalArgumentException) {
            logger.warn { "Malformed JWT claims: ${e.message}" }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired token")
            return
        }
        filterChain.doFilter(request, response)
    }
}
