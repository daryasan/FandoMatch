package org.example.controller

import org.example.service.ProfilesService
import org.springframework.web.bind.annotation.RestController

@RestController
class ProfilesController(
    private val profilesService: ProfilesService
)
