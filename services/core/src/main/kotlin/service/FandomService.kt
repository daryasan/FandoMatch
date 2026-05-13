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
import org.springframework.transaction.annotation.Transactional
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

    @Transactional
    fun fandomOneshot(request: FandomOneshotRequest): FandomOneshotResponse {
        val insertFromPending = request.insertFromPending ?: false
        val provided = request.fandoms ?: emptyList()

        // Collect items to process
        val itemsToInsert: List<FandomInput> = if (insertFromPending) {
            val pending = fandomRequestRepository.findAllByStatus("PENDING")
            if (provided.isEmpty()) {
                // All pending → convert to FandomInput, skip those with unknown/null category
                pending.mapNotNull { req ->
                    val cat = req.category?.let { runCatching { com.fandomatch.core.model.FandomCategory.valueOf(it) }.getOrNull() }
                        ?: return@mapNotNull null
                    FandomInput(name = req.name, category = cat)
                }
            } else {
                // Intersection: provided items that also exist in pending
                val pendingKeys = pending.map { it.name to it.category }.toSet()
                provided.filter { item -> pendingKeys.any { (n, c) -> n == item.name && c == item.category?.name } }
            }
        } else {
            provided
        }

        if (itemsToInsert.isEmpty()) {
            return FandomOneshotResponse(
                status = FandomOneshotStatus.SUCCESS,
                successResponse = FandomOneshotData(fandomsInserted = emptyList(), fandomsNotInserted = emptyList())
            )
        }

        // Validate all categories upfront
        val categoryCache = mutableMapOf<String, FandomCategory>()
        for (item in itemsToInsert) {
            val catName = item.category?.name ?: return FandomOneshotResponse(status = FandomOneshotStatus.CATEGORY_NOT_FOUND)
            if (catName !in categoryCache) {
                val cat = fandomCategoryRepository.findByName(catName)
                    ?: return FandomOneshotResponse(status = FandomOneshotStatus.CATEGORY_NOT_FOUND)
                categoryCache[catName] = cat
            }
        }

        // Insert fandoms
        val inserted = mutableListOf<FandomInput>()
        val notInserted = mutableListOf<FandomInput>()

        for (item in itemsToInsert) {
            val dbCategory = categoryCache[item.category!!.name]!!
            if (fandomRepository.findByCategoryIdAndName(dbCategory.id, item.name) != null) {
                notInserted.add(item)
            } else {
                fandomRepository.save(
                    org.example.models.db_models.Fandom(
                        id = UUID.randomUUID(),
                        categoryId = dbCategory.id,
                        name = item.name,
                        description = null
                    )
                )
                inserted.add(item)
            }
        }

        // Mark corresponding pending requests as APPROVED
        if (insertFromPending && inserted.isNotEmpty()) {
            val insertedKeys = inserted.map { it.name to it.category?.name }.toSet()
            val toApprove = fandomRequestRepository.findAllByStatus("PENDING")
                .filter { req -> insertedKeys.any { (n, c) -> n == req.name && c == req.category } }
                .mapNotNull { it.id }
            if (toApprove.isNotEmpty()) {
                fandomRequestRepository.updateStatusByIds(toApprove, "APPROVED")
            }
        }

        return FandomOneshotResponse(
            status = FandomOneshotStatus.SUCCESS,
            successResponse = FandomOneshotData(fandomsInserted = inserted, fandomsNotInserted = notInserted)
        )
    }
}
