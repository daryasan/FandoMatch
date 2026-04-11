package org.example.util

import com.fandomatch.messaging.model.*
import org.example.exceptions.BusinessException

fun getChatPreviewsErrorResponse(exception: BusinessException) = ChatPreviewsResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getChatErrorResponse(exception: BusinessException) = ChatResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getChatMessagesErrorResponse(exception: BusinessException) = ChatMessagesResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getSendMessageErrorResponse(exception: BusinessException) = SendMessageResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getPresignedUploadErrorResponse(exception: BusinessException) = PresignedUploadResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)
