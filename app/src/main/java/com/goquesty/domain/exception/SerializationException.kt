package com.goquesty.domain.exception

class SerializationException(
    message: String = "Failed to parse response",
    cause: Throwable? = null
) : Exception(message, cause)