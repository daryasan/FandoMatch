package org.example.controller

import org.example.service.PostsService
import org.springframework.web.bind.annotation.RestController

@RestController
class PostsController(
    private val postsService: PostsService
)
