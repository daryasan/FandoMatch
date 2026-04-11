package com.fandomatch.media

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Duration

class MediaServiceTest {

    private val s3Presigner: S3Presigner = mockk()
    private val config = MediaConfig(
        bucket = "test-bucket",
        region = "ru-central1",
        accessKey = "key",
        secretKey = "secret",
        endpointUrl = "https://storage.yandexcloud.net",
        uploadTtlMinutes = 5,
        downloadTtlMinutes = 60
    )

    private lateinit var mediaService: MediaService

    @BeforeEach
    fun setUp() {
        mediaService = MediaService(s3Presigner, config)
    }

    // --- generatePresignedUploadUrl ---

    @Test
    fun `generatePresignedUploadUrl returns URL from presigner`() {
        val presignedPut: PresignedPutObjectRequest = mockk()
        every { presignedPut.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/media-123?X-Amz-Signature=abc").toURL()
        every { s3Presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns presignedPut

        val result = mediaService.generatePresignedUploadUrl("media-123")

        assertThat(result).isEqualTo("https://storage.yandexcloud.net/test-bucket/media-123?X-Amz-Signature=abc")
        verify(exactly = 1) { s3Presigner.presignPutObject(any<PutObjectPresignRequest>()) }
    }

    @Test
    fun `generatePresignedUploadUrl uses correct bucket and key`() {
        val presignedPut: PresignedPutObjectRequest = mockk()
        every { presignedPut.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/my-file").toURL()
        val requestSlot = slot<PutObjectPresignRequest>()
        every { s3Presigner.presignPutObject(capture(requestSlot)) } returns presignedPut

        mediaService.generatePresignedUploadUrl("my-file")

        val capturedRequest = requestSlot.captured
        assertThat(capturedRequest.putObjectRequest().bucket()).isEqualTo("test-bucket")
        assertThat(capturedRequest.putObjectRequest().key()).isEqualTo("my-file")
    }

    @Test
    fun `generatePresignedUploadUrl applies upload TTL`() {
        val presignedPut: PresignedPutObjectRequest = mockk()
        every { presignedPut.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/file").toURL()
        val requestSlot = slot<PutObjectPresignRequest>()
        every { s3Presigner.presignPutObject(capture(requestSlot)) } returns presignedPut

        mediaService.generatePresignedUploadUrl("file")

        assertThat(requestSlot.captured.signatureDuration()).isEqualTo(Duration.ofMinutes(5))
    }

    // --- generateSignedDownloadUrl ---

    @Test
    fun `generateSignedDownloadUrl returns URL from presigner`() {
        val presignedGet: PresignedGetObjectRequest = mockk()
        every { presignedGet.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/media-456?X-Amz-Signature=xyz").toURL()
        every { s3Presigner.presignGetObject(any<GetObjectPresignRequest>()) } returns presignedGet

        val result = mediaService.generateSignedDownloadUrl("media-456")

        assertThat(result).isEqualTo("https://storage.yandexcloud.net/test-bucket/media-456?X-Amz-Signature=xyz")
        verify(exactly = 1) { s3Presigner.presignGetObject(any<GetObjectPresignRequest>()) }
    }

    @Test
    fun `generateSignedDownloadUrl uses correct bucket and key`() {
        val presignedGet: PresignedGetObjectRequest = mockk()
        every { presignedGet.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/img.jpg").toURL()
        val requestSlot = slot<GetObjectPresignRequest>()
        every { s3Presigner.presignGetObject(capture(requestSlot)) } returns presignedGet

        mediaService.generateSignedDownloadUrl("img.jpg")

        val capturedRequest = requestSlot.captured
        assertThat(capturedRequest.getObjectRequest().bucket()).isEqualTo("test-bucket")
        assertThat(capturedRequest.getObjectRequest().key()).isEqualTo("img.jpg")
    }

    @Test
    fun `generateSignedDownloadUrl applies download TTL`() {
        val presignedGet: PresignedGetObjectRequest = mockk()
        every { presignedGet.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/file").toURL()
        val requestSlot = slot<GetObjectPresignRequest>()
        every { s3Presigner.presignGetObject(capture(requestSlot)) } returns presignedGet

        mediaService.generateSignedDownloadUrl("file")

        assertThat(requestSlot.captured.signatureDuration()).isEqualTo(Duration.ofMinutes(60))
    }

    @Test
    fun `generateSignedDownloadUrl and generatePresignedUploadUrl use different TTLs`() {
        val presignedPut: PresignedPutObjectRequest = mockk()
        val presignedGet: PresignedGetObjectRequest = mockk()
        every { presignedPut.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/f").toURL()
        every { presignedGet.url() } returns URI.create("https://storage.yandexcloud.net/test-bucket/f").toURL()
        val putSlot = slot<PutObjectPresignRequest>()
        val getSlot = slot<GetObjectPresignRequest>()
        every { s3Presigner.presignPutObject(capture(putSlot)) } returns presignedPut
        every { s3Presigner.presignGetObject(capture(getSlot)) } returns presignedGet

        mediaService.generatePresignedUploadUrl("f")
        mediaService.generateSignedDownloadUrl("f")

        assertThat(putSlot.captured.signatureDuration()).isLessThan(getSlot.captured.signatureDuration())
    }
}
