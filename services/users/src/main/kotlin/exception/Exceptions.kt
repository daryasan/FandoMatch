package org.example.exception

abstract class BusinessException(val code: String, message: String) : Exception(message)

class UsernameAlreadyExistsException(username: String) :
    BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS.name, "Username $username already exists")

class InvalidUserInputData(message: String) :
    BusinessException(ErrorCode.INVALID_USER_DATA.name, "Some fields were invalid while creating user:$message")
