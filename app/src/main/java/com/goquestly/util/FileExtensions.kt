package com.goquestly.util

import java.io.File

val File.imageMimeType
    get() = when (extension.lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }