package com.fandomatch.media

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
@EnableConfigurationProperties(MediaConfig::class)
open class MediaAutoConfiguration(private val mediaConfig: MediaConfig) {

    @Bean
    @ConditionalOnMissingBean
    open fun s3Presigner(): S3Presigner {
        val credentials = AwsBasicCredentials.create(mediaConfig.accessKey, mediaConfig.secretKey)
        return S3Presigner.builder()
            .region(Region.of(mediaConfig.region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(URI.create(mediaConfig.endpointUrl))
            .build()
    }
}
