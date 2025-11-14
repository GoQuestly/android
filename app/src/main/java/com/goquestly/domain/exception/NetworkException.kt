package com.goquestly.domain.exception

class NetworkException(
    message: String = "No internet connection",
    cause: Throwable? = null
) : Exception(message, cause)