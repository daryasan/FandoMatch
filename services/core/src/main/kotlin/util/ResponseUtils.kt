package org.example.util

import com.fandomatch.core.model.EditUserProfileResponse
import com.fandomatch.core.model.Error
import com.fandomatch.core.model.ResponseStatus
import com.fandomatch.core.model.UserProfileResponse
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