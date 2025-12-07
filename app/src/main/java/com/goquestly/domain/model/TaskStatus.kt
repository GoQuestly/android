package com.goquestly.domain.model

enum class TaskStatus(val value: String) {
    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    COMPLETED_SUCCESS("completed_success"),
    COMPLETED_FAILED("completed_failed"),
    IN_REVIEW("in_review"),
    EXPIRED("expired");
}
