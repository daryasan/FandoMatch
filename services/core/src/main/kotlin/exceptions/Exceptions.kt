package org.example.exceptions

open class BusinessException(val code: String, message: String) : Exception(message)
open class InternalException(val code: String, message: String) : Exception(message)

class UsersNotRespondingException(errorCode : String) :
    InternalException(ErrorCode.USERS_NOT_RESPONDING.name, "Users API not responding with code: $errorCode")
