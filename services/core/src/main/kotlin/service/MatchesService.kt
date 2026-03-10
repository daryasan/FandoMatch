package org.example.service

import org.example.repository.MatchRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class MatchesService(
    private val matchRepository: MatchRepository
) {

    fun areFriends(user1: UUID, user2: UUID): Boolean {
        val (first, second) = if (user1 < user2) user1 to user2 else user2 to user1
        return matchRepository.existsByUserId1AndUserId2(first, second)
    }

}