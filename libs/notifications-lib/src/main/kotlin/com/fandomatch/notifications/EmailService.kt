package com.fandomatch.notifications

import io.github.oshai.kotlinlogging.KLogging
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class EmailService(
    private val mailSender: JavaMailSender,
    private val emailConfig: EmailConfig
) {

    companion object : KLogging()

    fun sendVerificationCode(toEmail: String, code: String) {
        val message = SimpleMailMessage()
        message.from = emailConfig.from
        message.setTo(toEmail)
        message.subject = "FandoMatch — код подтверждения"
        message.text = "Ваш код подтверждения: $code\n\nКод действителен 15 минут."
        try {
            mailSender.send(message)
            logger.info { "Verification code email sent to $toEmail" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send verification code email to $toEmail" }
            throw e
        }
    }
}
