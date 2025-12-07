package com.goquestly.domain.model

enum class ParticipationBlockReason(
    val value: String
) {
    NO_LOCATION("NO_LOCATION"),
    TOO_FAR_FROM_START("TOO_FAR_FROM_START"),
    REQUIRED_TASK_NOT_COMPLETED("REQUIRED_TASK_NOT_COMPLETED");
}
