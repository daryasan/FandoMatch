package com.fandomatch.media

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [MediaConfigTest.Config::class])
@TestPropertySource(properties = [
    "media.s3.bucket=my-bucket",
    "media.s3.region=ru-central1",
    "media.s3.access-key=AKID",
    "media.s3.secret-key=SECRET",
    "media.s3.endpoint-url=https://storage.yandexcloud.net",
    "media.s3.upload-ttl-minutes=10",
    "media.s3.download-ttl-minutes=120",
])
class MediaConfigTest {

    @EnableConfigurationProperties(MediaConfig::class)
    class Config

    @Autowired
    lateinit var mediaConfig: MediaConfig

    @Test
    fun `binds bucket`() {
        assertThat(mediaConfig.bucket).isEqualTo("my-bucket")
    }

    @Test
    fun `binds region`() {
        assertThat(mediaConfig.region).isEqualTo("ru-central1")
    }

    @Test
    fun `binds access key`() {
        assertThat(mediaConfig.accessKey).isEqualTo("AKID")
    }

    @Test
    fun `binds secret key`() {
        assertThat(mediaConfig.secretKey).isEqualTo("SECRET")
    }

    @Test
    fun `binds endpoint URL`() {
        assertThat(mediaConfig.endpointUrl).isEqualTo("https://storage.yandexcloud.net")
    }

    @Test
    fun `binds upload TTL`() {
        assertThat(mediaConfig.uploadTtlMinutes).isEqualTo(10L)
    }

    @Test
    fun `binds download TTL`() {
        assertThat(mediaConfig.downloadTtlMinutes).isEqualTo(120L)
    }

    @Test
    fun `upload TTL is less than download TTL by default`() {
        assertThat(mediaConfig.uploadTtlMinutes).isLessThan(mediaConfig.downloadTtlMinutes)
    }
}
