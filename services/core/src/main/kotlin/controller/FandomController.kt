package controller

import org.example.service.FandomService
import org.springframework.web.bind.annotation.RestController

@RestController
class FandomController(
    private val fandomService: FandomService
)

