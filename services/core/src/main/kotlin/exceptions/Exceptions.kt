package org.example.exceptions

open class BusinessException(val code: String, message: String) : Exception(message)

class UsernameAlreadyExistsException(username: String) :
    BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS.name, "Username $username already exists")

class InvalidUserInputData(message: String) :
    BusinessException(ErrorCode.INVALID_USER_DATA.name, "Some fields were invalid while creating user:$message")

class UserNotFoundException(byField: String) :
    BusinessException(ErrorCode.USER_NOT_FOUND.name, "User not found with by field value=$byField")

class UserCredentialMismatchException(credentialType: String) :
    BusinessException(
        ErrorCode.CREDENTIALS_MISMATCH.name,
        "User credential $credentialType mismatch"
    )

class UserCredentialNotFoundException(credentialType: String) :
    BusinessException(
        ErrorCode.CREDENTIAL_TYPE_NOT_FOUND.name,
        "User does not have credentials with type:$credentialType"
    )

class TokenRefreshingException(message: String) : BusinessException(
    ErrorCode.REFRESH_TOKEN_INVALID.name, message
)
