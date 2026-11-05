package ru.culture.house.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.culture.house.controller.dto.*
import ru.culture.house.exception.BadRequestException
import ru.culture.house.exception.NotFoundException
import ru.culture.house.persistence.entity.*
import ru.culture.house.persistence.repository.*
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EventService(
    private val events: EventRepository,
    private val clubs: ClubRepository,
    private val users: UserRepository,
    private val visitors: VisitorCategoryRepository,
    private val disabilities: DisabilityCategoryRepository,
) {
    @Transactional
    fun create(clubId: Long, request: EventRequest): EventResponse {
        val club = clubs.findById(clubId).orElseThrow { NotFoundException("Клуб с id=$clubId не найден") }
        val event = Event(club = club, name = request.name, eventDate = request.eventDate, type = EventType.FREE)
        applyRequest(event, request)
        return events.saveAndFlush(event).toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): EventResponse = find(id).toResponse()

    @Transactional(readOnly = true)
    fun getAll(clubId: Long, dateFrom: LocalDate?, dateTo: LocalDate?): List<EventResponse> {
        if (!clubs.existsById(clubId)) throw NotFoundException("Клуб с id=$clubId не найден")
        val from = dateFrom ?: LocalDate.of(1, 1, 1)
        val to = dateTo ?: LocalDate.of(9999, 12, 31)
        if (from > to) throw BadRequestException("Начало периода позже окончания")
        return events.findInPeriod(clubId, from, to).map { it.toResponse() }
    }

    @Transactional
    fun update(id: Long, request: EventRequest): EventResponse {
        val event = find(id)
        requireFree(event)
        applyRequest(event, request)
        return events.saveAndFlush(event).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val event = find(id)
        requireFree(event)
        events.delete(event)
        events.flush()
    }

    private fun find(id: Long): Event =
        events.findById(id).orElseThrow { NotFoundException("Мероприятие с id=$id не найдено") }

    private fun requireFree(event: Event) {
        if (event.type != EventType.FREE) throw BadRequestException("Платные мероприятия пока не поддерживаются")
    }

    private fun validateCounts(rows: List<AttendanceRequest>) {
        if (rows.any { it.count <= 0 }) throw BadRequestException("Посещаемость должна быть положительной")
        if (rows.map { it.categoryId }.distinct().size != rows.size) {
            throw BadRequestException("Категории посещаемости не должны повторяться")
        }
    }

    private fun applyRequest(event: Event, request: EventRequest) {
        if (request.name.isBlank() || request.name.trim().length > 255) {
            throw BadRequestException("Название должно содержать от 1 до 255 символов")
        }
        if ((request.venue?.length ?: 0) > 255) throw BadRequestException("Место проведения длиннее 255 символов")
        if (request.eventStartTime?.toLocalDate()?.let { it != request.eventDate } == true) {
            throw BadRequestException("Дата начала должна совпадать с датой мероприятия")
        }
        if (request.eventStartTime != null && request.eventEndTime != null &&
            request.eventEndTime < request.eventStartTime) {
            throw BadRequestException("Окончание мероприятия раньше начала")
        }
        validateCounts(request.attendance)
        validateCounts(request.disabilityAttendance)
        if (request.responsibleUserIds.distinct().size != request.responsibleUserIds.size) {
            throw BadRequestException("Ответственные не должны повторяться")
        }
        val responsible = request.responsibleUserIds.map { id ->
            val user = users.findById(id).orElseThrow { NotFoundException("Пользователь с id=$id не найден") }
            if (user.club?.id != event.club.id || !user.isActive) {
                throw BadRequestException("Ответственный должен быть активным пользователем клуба мероприятия")
            }
            user
        }
        val visitorRows = request.attendance.map { row ->
            val category = visitors.findById(row.categoryId)
                .orElseThrow { NotFoundException("Категория посетителей с id=${row.categoryId} не найдена") }
            category to row.count
        }
        val disabilityRows = request.disabilityAttendance.map { row ->
            val category = disabilities.findById(row.categoryId)
                .orElseThrow { NotFoundException("Категория инвалидности с id=${row.categoryId} не найдена") }
            category to row.count
        }

        event.name = request.name.trim()
        event.eventDate = request.eventDate
        event.eventStartTime = request.eventStartTime
        event.eventEndTime = request.eventEndTime
        event.status = request.status
        event.venue = request.venue?.trim()?.takeIf { it.isNotEmpty() }
        event.comment = request.comment
        event.updatedAt = LocalDateTime.now()

        event.responsibleUsers.removeIf { it.user.id !in request.responsibleUserIds }
        responsible.filter { user -> event.responsibleUsers.none { it.user.id == user.id } }
            .forEach { event.responsibleUsers.add(EventResponsibleUser(event = event, user = it)) }
        event.attendance.removeIf { existing -> visitorRows.none { it.first.id == existing.category.id } }
        visitorRows.forEach { (category, count) ->
            val existing = event.attendance.find { it.category.id == category.id }
            if (existing == null) event.attendance.add(EventAttendance(event = event, category = category, count = count))
            else existing.count = count
        }
        event.disabilityAttendance.removeIf { existing -> disabilityRows.none { it.first.id == existing.category.id } }
        disabilityRows.forEach { (category, count) ->
            val existing = event.disabilityAttendance.find { it.category.id == category.id }
            if (existing == null) {
                event.disabilityAttendance.add(EventDisabilityAttendance(event = event, category = category, count = count))
            } else existing.count = count
        }
    }
}
