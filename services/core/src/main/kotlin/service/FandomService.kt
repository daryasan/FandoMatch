package org.example.service

import com.fandomatch.core.model.FandomListResponse
import com.fandomatch.core.model.FandomListResponseFandomsInner
import org.springframework.stereotype.Service

@Service
class FandomService {

    private val fandoms = mutableListOf(
        FandomListResponseFandomsInner(
            id = "1",
            name = "My Chemical Romance",
        ),
        FandomListResponseFandomsInner(
            id = "2",
            name = "Harry Potter",
        ),
        FandomListResponseFandomsInner(
            id = "3",
            name = "Marvel",
        )
    )

    /**
     * Возвращает список всех фандомов
     */
    fun getAllFandoms(): FandomListResponse {
        return FandomListResponse(fandoms = fandoms)
    }

    /**
     * Возвращает один фандом по ID
     */
    fun getFandomById(id: String): FandomListResponseFandomsInner? {
        return fandoms.find { it.id == id }
    }

    /**
     * Создаёт новый фандом (мок)
     */
    fun createFandom(name: String, description: String?): FandomListResponseFandomsInner {
        val newFandom = FandomListResponseFandomsInner(
            id = (fandoms.size + 1).toString(),
            name = name,
            description = description ?: ""
        )
        fandoms.add(newFandom)
        return newFandom
    }
}
