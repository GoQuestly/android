package com.goquestly.domain.exception

open class HttpException(
    val code: Int,
    val errorMessage: String,
    val errorBody: String? = null
) : Exception("HTTP $code: $errorMessage")

class BadRequestException(
    message: String = "Invalid request data",
    errorBody: String? = null
) : HttpException(400, message, errorBody)

class UnauthorizedException(
    message: String = "Authentication required",
    errorBody: String? = null
) : HttpException(401, message, errorBody)

class ForbiddenException(
    message: String = "Access denied",
    errorBody: String? = null
) : HttpException(403, message, errorBody)

class NotFoundException(
    message: String = "Resource not found",
    errorBody: String? = null
) : HttpException(404, message, errorBody)

class ConflictException(
    message: String = "Resource conflict",
    errorBody: String? = null
) : HttpException(409, message, errorBody)

class InternalServerErrorException(
    code: Int = 500,
    message: String = "Server encountered an error",
    errorBody: String? = null
) : HttpException(code, message, errorBody)
