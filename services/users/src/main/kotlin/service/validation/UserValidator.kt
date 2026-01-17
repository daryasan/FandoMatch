package org.example.service.validation

import org.example.exception.UserInactiveException
import org.example.model.db_models.User
import org.example.model.db_models.UserStatus
import org.example.service.AuthService.Companion.logger
import org.springframework.stereotype.Component

@Component
class UserValidator {

    fun validateUserBeforeLogin(user: User) {
        if (user.status != UserStatus.ACTIVE) {
            logger.error { "User is inactive: current user status is ${user.status}" }
            throw UserInactiveException(user.status)
        }
    }

}