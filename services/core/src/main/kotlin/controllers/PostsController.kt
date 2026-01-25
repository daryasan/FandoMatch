package org.example.core.controller

import com.fandomatch.core.api.PostsApi
import com.fandomatch.core.model.CreatePostRequest
import com.fandomatch.core.model.CreatePostResponse
import com.fandomatch.core.model.PostListResponse
import com.fandomatch.core.model.PostsGetRequest
import org.example.service.PostsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class PostsController(
    private val postsService: PostsService
) : PostsApi {

    override fun corePostsCreatePost(
        authorization: String,
        createPostRequest: CreatePostRequest
    ): ResponseEntity<CreatePostResponse> {
        val created = postsService.createPost(createPostRequest)
        return ResponseEntity.ok(created)
    }

    override fun corePostsGetPost(postsGetRequest: PostsGetRequest): ResponseEntity<PostListResponse> {
        val posts = postsService.getAllPosts()
        return ResponseEntity.ok(posts)
    }
}
