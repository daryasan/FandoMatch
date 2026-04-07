package org.example.utils

import com.fandomatch.users.model.*
import org.example.exception.BusinessException

fun getErrorRegistrationResponse(exception: BusinessException) = UserRegistrationResponse(
    status = ResponseStatus.ERROR, errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getErrorLoginResponse(exception: BusinessException) = UserLoginResponse(
    status = ResponseStatus.ERROR, errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getErrorChangePasswordResponse(exception: BusinessException) = ChangePasswordResponse(
    status = ResponseStatus.ERROR, errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getRefreshTokenErrorResponse(exception: BusinessException) = RefreshTokenResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)

fun getUserCredentialsErrorResponse(exception: BusinessException) = GetUserCredentialsResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message
    )
)