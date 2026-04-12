package com.fandomatch.notifications

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [NotificationsConfigTest.Config::class])
@TestPropertySource(
    properties = [
        "notifications.firebase.credentials-json={\"type\":\"service_account\"}",
        "notifications.firebase.project-id=test-project-id",
    ]
)
class NotificationsConfigTest {

    @EnableConfigurationProperties(NotificationsConfig::class)
    class Config

    @Autowired
    lateinit var config: NotificationsConfig

    @Test
    fun `binds credentials json`() {
        assertThat(config.credentialsJson).isEqualTo("{\"type\":\"service_account\"}")
    }

    @Test
    fun `binds project id`() {
        assertThat(config.projectId).isEqualTo("test-project-id")
    }

    @Test
    fun `credentials json is not blank`() {
        assertThat(config.credentialsJson).isNotBlank()
    }

    @Test
    fun `project id is not blank`() {
        assertThat(config.projectId).isNotBlank()
    }
}
