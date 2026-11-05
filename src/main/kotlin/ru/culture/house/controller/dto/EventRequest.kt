package ru.culture.house.controller.dto

import ru.culture.house.persistence.entity.EventStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class AttendanceRequest(val categoryId: Long, val count: Int)

data class EventRequest(
    val name: String,
    val eventDate: LocalDate,
    val eventStartTime: LocalDateTime? = null,
    val eventEndTime: LocalDateTime? = null,
    val status: EventStatus = EventStatus.PLANNED,
    val venue: String? = null,
    val comment: String? = null,
    val responsibleUserIds: List<Long> = emptyList(),
    val attendance: List<AttendanceRequest> = emptyList(),
    val disabilityAttendance: List<AttendanceRequest> = emptyList(),
)
