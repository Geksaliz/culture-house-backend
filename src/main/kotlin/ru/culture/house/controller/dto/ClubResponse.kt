package ru.culture.house.controller.dto

import ru.culture.house.persistence.entity.Club
import java.time.LocalDateTime

data class ClubResponse(
    val id: Long,
    val name: String,
    val address: String?,
    val createdAt: LocalDateTime
)

fun Club.toResponse() = ClubResponse(
    id = requireNotNull(id) { "Клуб ещё не сохранён" },
    name = name,
    address = address,
    createdAt = createdAt,
)