package com.goquestly.domain.model

enum class ParticipationStatus(
    val blocksSessionProgression: Boolean = false
) {
    PENDING(blocksSessionProgression = false),
    APPROVED(blocksSessionProgression = false),
    REJECTED(blocksSessionProgression = true),
    DISQUALIFIED(blocksSessionProgression = true)
}
