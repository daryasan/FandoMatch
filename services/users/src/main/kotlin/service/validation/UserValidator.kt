package org.example.service.validation

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.exception.UserInactiveException
import org.example.model.db_models.User
import org.example.model.db_models.enums.UserStatus
import org.springframework.stereotype.Component

@Component
class UserValidator {

    private val logger = KotlinLogging.logger {}


    fun validateUserBeforeLogin(user: User) {
        if (user.status != UserStatus.ACTIVE) {
            logger.error { "User is inactive: current user status is ${user.status}" }
            throw UserInactiveException(user.status)
        }
    }

}