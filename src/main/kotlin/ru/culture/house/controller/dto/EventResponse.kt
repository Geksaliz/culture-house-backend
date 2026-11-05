package ru.culture.house.controller.dto

import ru.culture.house.persistence.entity.Event
import ru.culture.house.persistence.entity.EventStatus
import ru.culture.house.persistence.entity.EventType
import java.time.LocalDate
import java.time.LocalDateTime

data class EventResponse(
    val id: Long,
    val clubId: Long,
    val name: String,
    val eventDate: LocalDate,
    val eventStartTime: LocalDateTime?,
    val eventEndTime: LocalDateTime?,
    val type: EventType,
    val status: EventStatus,
    val venue: String?,
    val comment: String?,
    val responsibleUserIds: List<Long>,
    val attendance: List<AttendanceRequest>,
    val disabilityAttendance: List<AttendanceRequest>,
    val totalVisitors: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun Event.toResponse() = EventResponse(
    id = requireNotNull(id),
    clubId = requireNotNull(club.id),
    name = name,
    eventDate = eventDate,
    eventStartTime = eventStartTime,
    eventEndTime = eventEndTime,
    type = type,
    status = status,
    venue = venue,
    comment = comment,
    responsibleUserIds = responsibleUsers.map { requireNotNull(it.user.id) }.sorted(),
    attendance = attendance.map { AttendanceRequest(requireNotNull(it.category.id), it.count) }
        .sortedBy { it.categoryId },
    disabilityAttendance = disabilityAttendance.map { AttendanceRequest(requireNotNull(it.category.id), it.count) }
        .sortedBy { it.categoryId },
    totalVisitors = attendance.sumOf { it.count.toLong() },
    createdAt = createdAt,
    updatedAt = updatedAt,
)
