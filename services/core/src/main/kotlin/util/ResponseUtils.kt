package org.example.util

import com.fandomatch.core.model.*
import org.example.exceptions.BusinessException

fun getUserProfileErrorResponse(exception: BusinessException) = UserProfileResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getEditUserProfileErrorResponse(exception: BusinessException) = EditUserProfileResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getMatchCandidateBatchErrorResponse(exception: BusinessException) = MatchCandidateBatchResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getMatchActionErrorResponse(exception: BusinessException) = MatchActionResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getMatchFilterErrorResponse(exception: BusinessException) = MatchFilterResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getCurrentFiltersErrorResponse(exception: BusinessException) = CurrentFiltersResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getPostListErrorResponse(exception: BusinessException) = PostListResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getCreatePostErrorResponse(exception: BusinessException) = CreatePostResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getExtendedPostErrorResponse(exception: BusinessException) = ExtendedPostResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getCommentListErrorResponse(exception: BusinessException) = CommentListResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getPostLikeErrorResponse(exception: BusinessException) = PostLikeResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getFandomListErrorResponse(exception: BusinessException) = FandomListResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getFandomCategoryListErrorResponse(exception: BusinessException) = FandomCategoryListResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getFandomRequestCreateErrorResponse(exception: BusinessException) = FandomRequestCreateResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getCreateCommentErrorResponse(exception: BusinessException) = CreateCommentResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getPendingRequestsErrorResponse(exception: BusinessException) = PendingRequestsResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)

fun getFandomOneshotErrorResponse(@Suppress("UNUSED_PARAMETER") exception: BusinessException) = FandomOneshotResponse(
    status = FandomOneshotStatus.CATEGORY_NOT_FOUND
)

fun getFriendsErrorResponse(exception: BusinessException) = FriendsResponse(
    status = ResponseStatus.ERROR,
    errorResponse = Error(
        errorCode = exception.code,
        errorMessage = exception.message,
    )
)