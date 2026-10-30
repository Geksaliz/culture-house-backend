package ru.culture.house.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.culture.house.controller.dto.CreateUserRequest
import ru.culture.house.controller.dto.UserResponse
import ru.culture.house.service.UserService

@RestController
@RequestMapping("/api/{version}/users")
// TODO: Добавить возможность деактивировать пользователей
class UserController(
    private val userService: UserService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateUserRequest): UserResponse =
        userService.create(request)
}
