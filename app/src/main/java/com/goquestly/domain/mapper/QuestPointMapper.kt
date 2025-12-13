package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.QuestPointDto
import com.goquestly.domain.model.QuestPoint
import com.goquestly.domain.model.TaskStatus

fun QuestPointDto.toDomainModel() = QuestPoint(
    pointId = pointId,
    name = pointName,
    orderNumber = orderNumber,
    isPassed = isPassed,
    latitude = pointLatitude,
    longitude = pointLongitude,
    hasTask = hasTask,
    isTaskSuccessCompletionRequiredForNextPoint = isTaskSuccessCompletionRequiredForNextPoint,
    taskStatus = TaskStatus.entries.find { it.value == taskStatus }
)
