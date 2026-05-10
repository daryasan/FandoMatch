package com.fandomatch.notifications

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PushNotificationServiceTest {

    private val firebaseApp: FirebaseApp = mockk()
    private val firebaseMessaging: FirebaseMessaging = mockk()
    private lateinit var service: PushNotificationService

    @BeforeEach
    fun setUp() {
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance(firebaseApp) } returns firebaseMessaging
        service = PushNotificationService(firebaseApp)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `send calls FirebaseMessaging send`() {
        every { firebaseMessaging.send(any()) } returns "projects/test/messages/msg-id"

        service.send("fcm-token-abc", "Test Title", "Test Body")

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `send with extra data calls FirebaseMessaging send`() {
        every { firebaseMessaging.send(any()) } returns "projects/test/messages/msg-id"

        service.send("fcm-token-abc", "Title", "Body", mapOf("key1" to "val1", "key2" to "val2"))

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `send with empty data map calls FirebaseMessaging send`() {
        every { firebaseMessaging.send(any()) } returns "projects/test/messages/msg-id"

        service.send("fcm-token-abc", "Title", "Body", emptyMap())

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `send does not throw when FirebaseMessagingException occurs`() {
        val exception = mockk<FirebaseMessagingException>(relaxed = true)
        every { firebaseMessaging.send(any()) } throws exception

        assertThatNoException().isThrownBy {
            service.send("bad-token", "Title", "Body")
        }
    }

    @Test
    fun `send does not throw when FirebaseMessagingException occurs with data`() {
        val exception = mockk<FirebaseMessagingException>(relaxed = true)
        every { firebaseMessaging.send(any()) } throws exception

        assertThatNoException().isThrownBy {
            service.send("bad-token", "Title", "Body", mapOf("k" to "v"))
        }
    }

    @Test
    fun `send uses default empty data map`() {
        every { firebaseMessaging.send(any()) } returns "msg-id"

        service.send("token", "Hello", "World")

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `sendDataMessage sends message without notification`() {
        every { firebaseMessaging.send(any()) } returns "msg-id"

        service.sendDataMessage("fcm-token", mapOf("type" to "chat", "chatId" to "abc"))

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `sendDataMessage does not throw when FirebaseMessagingException occurs`() {
        val exception = mockk<FirebaseMessagingException>(relaxed = true)
        every { firebaseMessaging.send(any()) } throws exception

        assertThatNoException().isThrownBy {
            service.sendDataMessage("bad-token", mapOf("type" to "match"))
        }
    }
}
