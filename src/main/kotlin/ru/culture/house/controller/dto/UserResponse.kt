package ru.culture.house.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import ru.culture.house.persistence.entity.User
import ru.culture.house.persistence.entity.UserRole
import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val login: String,
    val role: UserRole,
    val clubId: Long?,
    @get:JsonProperty("isActive")
    val isActive: Boolean,
    val createdAt: LocalDateTime,
)

fun User.toResponse() = UserResponse(
    id = requireNotNull(id) { "Пользователь ещё не сохранён" },
    login = login,
    role = role,
    clubId = club?.id,
    isActive = isActive,
    createdAt = createdAt,
)