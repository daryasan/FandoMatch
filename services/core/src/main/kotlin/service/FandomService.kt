package org.example.service

import com.fandomatch.core.model.*
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.FandomRequest
import org.example.repository.FandomCategoryRepository
import org.example.repository.FandomRepository
import org.example.repository.FandomRequestRepository
import org.example.repository.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class FandomService(
    private val fandomRepository: FandomRepository,
    private val fandomCategoryRepository: FandomCategoryRepository,
    private val fandomRequestRepository: FandomRequestRepository,
    private val userProfileRepository: UserProfileRepository
) {

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
    }

    fun getFandoms(userId: UUID): List<Fandom> = fandomRepository.findAllByUserId(userId).map { it.toDto() }

    fun getAllFandoms() = fandomRepository.findAll()

    fun getUserFandoms(username: String): FandomListResponse {
        val userProfile = userProfileRepository.findByUsername(username)
            .orElseThrow { UserNotFoundException(username) }

        val fandoms = fandomRepository.findAllByUserId(userProfile.userId).map { it.toDto() }

        return FandomListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomListData(fandoms)
        )
    }

    fun getAllFandomsPaginated(page: Int?, size: Int?): FandomListResponse {
        val pageable = PageRequest.of(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        val fandoms = fandomRepository.findAll(pageable).content.map { it.toDto() }

        return FandomListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomListData(fandoms)
        )
    }

    fun getCategories(): FandomCategoryListResponse {
        val categories = fandomCategoryRepository.findAll().map { it.name }

        return FandomCategoryListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomCategoryListData(categories)
        )
    }

    fun requestNewFandom(request: FandomRequestCreate): FandomRequestCreateResponse {
        val fandomRequest = FandomRequest(
            name = request.name,
            description = request.description,
            category = request.category,
            authorUsername = request.authorUsername
        )

        fandomRequestRepository.save(fandomRequest)

        return FandomRequestCreateResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FandomRequestCreateSuccess(
                status = FandomRequestCreateSuccess.Status.RECEIVED
            )
        )
    }

    private fun org.example.models.db_models.Fandom.toDto() = Fandom(
        id = id.toString(),
        name = name,
        description = description
    )
}
