package org.example.service

import com.fandomatch.core.model.CreatePostRequest
import com.fandomatch.core.model.CreatePostResponse
import com.fandomatch.core.model.PostListResponse
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class PostsService {

    private val posts = ConcurrentHashMap<String, CreatePostResponse>()

    fun getAllPosts(): PostListResponse {
        return PostListResponse(posts = posts.values.toList())
    }

    fun getPostById(id: String): CreatePostResponse? {
        return posts[id]
    }

    fun createPost(req: CreatePostRequest): CreatePostResponse {
        val id = UUID.randomUUID().toString()

        val post = CreatePostResponse(
            id = id,
            title = req.title,
            content = req.content,
            createdAt = OffsetDateTime.now()
        )

        posts[id] = post
        return post
    }
}
