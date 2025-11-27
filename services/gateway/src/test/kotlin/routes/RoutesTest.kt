package org.example.routes

import org.example.BaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.route.RouteLocator

class RoutesTest : BaseTest() {

    @Autowired
    lateinit var routeLocator: RouteLocator

    @Test
    fun `route users-auth exists and has correct uri`() {
        val routes = routeLocator.routes.collectList().block()
        assertNotNull(routes)

        val usersRoute = routes!!.find { it.id == "users-auth" }
        assertNotNull(usersRoute)
        assertEquals("http://localhost:8081", usersRoute?.uri.toString())
    }
}
