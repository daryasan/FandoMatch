package org.example.exceptions

open class BusinessException(val code: String, message: String) : Exception(message)
open class InternalException(val code: String, message: String) : Exception(message)

class ChatNotFoundException(id: String) :
    BusinessException(ErrorCode.CHAT_NOT_FOUND.name, "Chat $id not found")

class CannotChatWithSelfException :
    BusinessException(ErrorCode.CANNOT_CHAT_WITH_SELF.name, "Cannot start a chat with yourself")

class NoMatchException(userId1: java.util.UUID, userId2: java.util.UUID) :
    BusinessException(ErrorCode.NO_MATCH.name, "No match between $userId1 and $userId2")

class UsersNotRespondingException(detail: String) :
    InternalException(ErrorCode.USERS_NOT_RESPONDING.name, "Users API not responding: $detail")
