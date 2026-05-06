package org.example.service

import com.fandomatch.notifications.EmailService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.example.exception.InvalidVerificationCodeException
import org.example.model.db_models.VerificationCode
import org.example.repository.VerificationCodeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class VerificationCodeService(
    private val verificationCodeRepository: VerificationCodeRepository,
    @Autowired(required = false) private val emailService: EmailService?,
) {

    private val logger = KotlinLogging.logger {}

    companion object {
        private const val CODE_LENGTH = 6
        private const val CODE_TTL_MINUTES = 15L
    }

    @Transactional
    fun sendCode(email: String) {
        val code = generateCode()
        val expiresAt = Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES)

        val entity = VerificationCode(
            email = email,
            code = code,
            expiresAt = expiresAt,
        )
        verificationCodeRepository.save(entity)
        logger.info { "Saved verification code for email=$email" }

        if (emailService != null) {
            emailService.sendVerificationCode(email, code)
        } else {
            logger.warn { "EmailService is not configured — verification code for $email: $code" }
        }
    }

    fun checkCode(email: String, code: String): Boolean {
        val record = verificationCodeRepository
            .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(email, Instant.now())
            ?: throw InvalidVerificationCodeException()

        if (record.code != code) {
            throw InvalidVerificationCodeException()
        }

        logger.info { "Verification code check passed for email=$email" }
        return true
    }

    @Transactional
    fun verifyAndConsume(email: String, code: String) {
        val record = verificationCodeRepository
            .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(email, Instant.now())
            ?: throw InvalidVerificationCodeException()

        if (record.code != code) {
            throw InvalidVerificationCodeException()
        }

        record.used = true
        verificationCodeRepository.save(record)
        logger.info { "Verification code consumed successfully for email=$email" }
    }

    private fun generateCode(): String {
        val random = SecureRandom()
        val number = random.nextInt(1_000_000)
        return number.toString().padStart(CODE_LENGTH, '0')
    }
}
