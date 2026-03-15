package org.example.util

import com.fandomatch.core.model.*
import org.example.exceptions.BusinessException

fun getUserProfileErrorResponse(exception: BusinessException) = UserProfileResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getEditUserProfileErrorResponse(exception: BusinessException) = EditUserProfileResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getMatchCandidateBatchErrorResponse(exception: BusinessException) = MatchCandidateBatchResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)