package org.example

import org.example.routes.Routes
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [
        Routes::class,
        GatewayApplication::class
    ]
)
@ActiveProfiles("test")
open class BaseTest