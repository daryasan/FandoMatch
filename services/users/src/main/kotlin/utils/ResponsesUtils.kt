package org.example.utils

import com.fandomatch.users.model.Error
import com.fandomatch.users.model.ResponseStatus
import com.fandomatch.users.model.UserRegistrationResponse
import org.example.exception.BusinessException

fun getErrorRegistrationResponse(exception: BusinessException) = UserRegistrationResponse(
    status = ResponseStatus.ERROR, errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)