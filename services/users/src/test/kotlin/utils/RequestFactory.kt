package utils

import com.fandomatch.users.model.UserLoginRequest
import com.fandomatch.users.model.UserRegistrationRequest

fun userRegistrationRequest(
    email: String = Constants.EMAIL,
    username: String = Constants.USERNAME,
    password: String = Constants.PASSWORD,
    name: String = Constants.NAME,
    birthDate: Long = Constants.BIRTH_DATE,
) = UserRegistrationRequest(
    email = email,
    username = username,
    hashedPassword = password,
    name = name,
    birthDate = birthDate,
)

fun userLoginRequest(
    email: String? = Constants.EMAIL,
    username: String = Constants.USERNAME,
    password: String = Constants.PASSWORD
) = UserLoginRequest(
    email = email,
    username = username,
    hashedPassword = password
)
