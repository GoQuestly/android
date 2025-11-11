package com.goquestly.util

import com.goquestly.domain.exception.BadRequestException
import com.goquestly.domain.exception.ConflictException
import com.goquestly.domain.exception.ForbiddenException
import com.goquestly.domain.exception.HttpException
import com.goquestly.domain.exception.InternalServerErrorException
import com.goquestly.domain.exception.NetworkException
import com.goquestly.domain.exception.NotFoundException
import com.goquestly.domain.exception.SerializationException
import com.goquestly.domain.exception.UnauthorizedException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


typealias RetrofitHttpException = retrofit2.HttpException
typealias KotlinSerializationException = kotlinx.serialization.SerializationException

fun <T> Result<T>.mapToAppException(): Result<T> = onFailure { error ->
    val appException = when (error) {
        is RetrofitHttpException -> {
            val errorBody = error.response()?.errorBody()?.string()

            when (error.code()) {
                400 -> BadRequestException(
                    message = errorBody ?: "Invalid request data",
                    errorBody = errorBody
                )

                401 -> UnauthorizedException(
                    message = errorBody ?: "Unauthorized",
                    errorBody = errorBody
                )

                403 -> ForbiddenException(
                    message = errorBody ?: "Access forbidden",
                    errorBody = errorBody
                )

                404 -> NotFoundException(
                    message = errorBody ?: "Resource not found",
                    errorBody = errorBody
                )

                409 -> ConflictException(
                    message = errorBody ?: "Resource already exists",
                    errorBody = errorBody
                )

                in 500..599 -> InternalServerErrorException(
                    code = error.code(),
                    message = errorBody ?: "Server error",
                    errorBody = errorBody
                )

                else -> HttpException(
                    code = error.code(),
                    errorMessage = errorBody ?: "HTTP error",
                    errorBody = errorBody
                )
            }
        }

        is UnknownHostException -> NetworkException(
            message = "No internet connection",
            cause = error
        )

        is SocketTimeoutException -> NetworkException(
            message = "Connection timeout",
            cause = error
        )

        is IOException -> NetworkException(
            message = "Network error: ${error.message}",
            cause = error
        )

        is KotlinSerializationException -> SerializationException(
            message = "Failed to parse response",
            cause = error
        )

        is HttpException,
        is NetworkException,
        is SerializationException -> error

        else -> Exception("An unexpected error occurred", error)
    }

    return Result.failure(appException)
}

suspend fun <T> runCatchingAppException(block: suspend () -> T): Result<T> {
    return runCatching {
        block()
    }.mapToAppException()
}