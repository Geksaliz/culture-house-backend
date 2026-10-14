package ru.culture.house.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.culture.house.controller.dto.ClubRequest
import ru.culture.house.controller.dto.ClubResponse
import ru.culture.house.service.ClubService

@RestController
@RequestMapping("/api/{version}/clubs")
class ClubController(
    private val clubService: ClubService,
) {

    @PostMapping(version = "1")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: ClubRequest): ClubResponse =
        clubService.create(request)

    @PutMapping("/{id}", version = "1")
    fun update(@PathVariable id: Long, @RequestBody request: ClubRequest): ClubResponse =
        clubService.update(id, request)

    @DeleteMapping("/{id}", version = "1")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) =
        clubService.delete(id)
}