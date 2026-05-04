package org.example.service

import com.fandomatch.core.model.*
import org.example.exceptions.FandomCategoryNotFoundException
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.FandomCategory
import org.example.models.db_models.FandomRequest
import org.example.repository.FandomCategoryRepository
import org.example.repository.FandomRepository
import org.example.repository.FandomRequestRepository
import org.example.repository.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class FandomService(
    private val fandomRepository: FandomRepository,
    private val fandomCategoryRepository: FandomCategoryRepository,
    private val fandomRequestRepository: FandomRequestRepository,
) {

    fun getFandoms(userId: UUID): List<Fandom> = fandomRepository.findAllByUserId(userId).map {
        it.toDto(
            fandomCategoryRepository.findById(it.categoryId).orElseThrow { FandomCategoryNotFoundException(it.categoryId.toString()) }
        )
    }

    fun getUserFandoms(uuid: String): FandomListResponse {
        val fandoms = getFandoms(UUID.fromString(uuid))

        return FandomListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomListData(fandoms)
        )
    }

    fun getCategories(): FandomCategoryListResponse {
        val categories = fandomCategoryRepository.findAll().map {
            com.fandomatch.core.model.FandomCategory.valueOf(it.name)
        }

        return FandomCategoryListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomCategoryListData(categories)
        )
    }

    fun searchFandoms(query: String): FandomListResponse {
        val fandoms = fandomRepository.findByNameContainingIgnoreCase(query).map {
            it.toDto(
                fandomCategoryRepository.findById(it.categoryId)
                    .orElseThrow { FandomCategoryNotFoundException(it.categoryId.toString()) }
            )
        }
        return FandomListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomListData(fandoms)
        )
    }

    fun requestNewFandom(request: FandomRequestCreate): FandomRequestCreateResponse {
        val fandomRequest = FandomRequest(
            name = request.name,
            description = request.description,
            category = request.category.name,
            authorUuid = request.authorUuid,
            createdAt = Instant.now()
        )

        fandomRequestRepository.save(fandomRequest)

        return FandomRequestCreateResponse(
            status = ResponseStatus.SUCCESS
        )
    }

    private fun org.example.models.db_models.Fandom.toDto(category: FandomCategory) = Fandom(
        id = id.toString(),
        name = name,
        category = com.fandomatch.core.model.FandomCategory.valueOf(category.name)
    )
}
