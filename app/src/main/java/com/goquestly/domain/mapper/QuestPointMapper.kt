package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.QuestPointDto
import com.goquestly.domain.model.QuestPoint

fun QuestPointDto.toDomainModel() = QuestPoint(
    name = pointName,
    orderNumber = orderNumber,
    isPassed = isPassed,
    latitude = pointLatitude,
    longitude = pointLongitude
)
