package org.example.service

import com.fandomatch.media.MediaService
import com.fandomatch.messaging.model.*
import org.example.models.db_models.MediaItemRecord
import org.example.repository.MediaItemRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class MediaPresignService(
    private val mediaService: MediaService,
    private val mediaItemRepository: MediaItemRepository
) {

    fun generateUploadUrl(mediaType: MediaType): PresignedUploadResponse {
        val mediaId = UUID.randomUUID().toString()
        val uploadUrl = mediaService.generatePresignedUploadUrl(mediaId)
        val expiresAt = Instant.now().plusSeconds(5 * 60).toEpochMilli()

        mediaItemRepository.save(MediaItemRecord(mediaId = mediaId, mediaType = mediaType.value))

        return PresignedUploadResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PresignedUploadData(
                mediaId = mediaId,
                uploadUrl = uploadUrl,
                expiresAt = expiresAt
            )
        )
    }
}
