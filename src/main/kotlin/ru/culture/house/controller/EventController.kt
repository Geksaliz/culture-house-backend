package ru.culture.house.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.culture.house.controller.dto.EventRequest
import ru.culture.house.controller.dto.EventResponse
import ru.culture.house.service.EventService
import java.time.LocalDate

@RestController
@RequestMapping("/api/{version}/events")
class EventController(private val eventService: EventService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestParam clubId: Long, @RequestBody request: EventRequest): EventResponse =
        eventService.create(clubId, request)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): EventResponse = eventService.getById(id)

    @GetMapping
    fun getAll(
        @RequestParam clubId: Long,
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
    ): List<EventResponse> = eventService.getAll(clubId, dateFrom, dateTo)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: EventRequest): EventResponse =
        eventService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = eventService.delete(id)
}
