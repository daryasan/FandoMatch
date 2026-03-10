package org.example.util

import io.github.oshai.kotlinlogging.KLogger
import org.example.exceptions.BusinessException
import org.springframework.http.ResponseEntity

inline fun <T : Any> onControllerRequest(
    logger: KLogger,
    operationName: String,
    errorMapper: (BusinessException) -> T,
    block: () -> T
): ResponseEntity<T> {
    logger.info("$operationName called")
    return try {
        ResponseEntity.ok(block())
    } catch (e: BusinessException) {
        logger.error("$operationName failed with error ${e.code}: ${e.message}")
        ResponseEntity.ok(errorMapper(e))
    }
}