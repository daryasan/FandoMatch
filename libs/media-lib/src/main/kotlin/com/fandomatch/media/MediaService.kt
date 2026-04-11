package com.fandomatch.media

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.Duration

@Service
class MediaService(
    private val s3Presigner: S3Presigner,
    private val mediaConfig: MediaConfig
) {

    fun generatePresignedUploadUrl(mediaId: String): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(mediaConfig.bucket)
            .key(mediaId)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(mediaConfig.uploadTtlMinutes))
            .putObjectRequest(putObjectRequest)
            .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }

    fun generateSignedDownloadUrl(mediaId: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(mediaConfig.bucket)
            .key(mediaId)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(mediaConfig.downloadTtlMinutes))
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }
}
