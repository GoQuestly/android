package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CodeWordSubmitDto(
    val codeWord: String
)