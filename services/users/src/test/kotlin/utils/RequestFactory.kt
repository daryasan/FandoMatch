package utils

import com.fandomatch.users.model.UserLoginRequest
import com.fandomatch.users.model.UserRegistrationRequest

fun userRegistrationRequest(
    email: String? = Constants.EMAIL,
    phone: String? = Constants.PHONE,
    username: String = Constants.USERNAME,
    password: String = Constants.PASSWORD
) = UserRegistrationRequest(
    email = email,
    phone = phone,
    username = username,
    hashedPassword = password
)

fun userLoginRequest(
    email: String? = Constants.EMAIL,
    phone: String? = Constants.PHONE,
    username: String = Constants.USERNAME,
    password: String = Constants.PASSWORD
) = UserLoginRequest(
    email = email,
    phone = phone,
    username = username,
    hashedPassword = password
)