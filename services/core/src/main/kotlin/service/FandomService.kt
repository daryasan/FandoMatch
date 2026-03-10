package org.example.service

import com.fandomatch.core.model.Fandom
import org.example.repository.FandomRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class FandomService(private val fandomRepository: FandomRepository) {

    fun getFandoms(userId: UUID): List<Fandom> = fandomRepository.findAllByUserId(userId).map { fandom ->
        Fandom(
            id = fandom.id.toString(),
            name = fandom.name,
            description = fandom.description
        )
    }

}
