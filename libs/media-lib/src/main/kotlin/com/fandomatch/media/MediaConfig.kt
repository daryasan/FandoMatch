package com.fandomatch.media

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("media.s3")
data class MediaConfig(
    val bucket: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val endpointUrl: String = "https://storage.yandexcloud.net",
    val uploadTtlMinutes: Long = 5,
    val downloadTtlMinutes: Long = 60
)
