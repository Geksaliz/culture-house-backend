package ru.culture.house.controller.dto

import ru.culture.house.persistence.entity.UserRole

/**
 * clubId обязателен для ADMIN и EMPLOYEE и должен отсутствовать у SUPER_ADMIN —
 * это бизнес-правило проверяется в UserService.
 */
data class CreateUserRequest(
    val login: String,
    val password: String,
    val role: UserRole,
    val clubId: Long? = null,
) {

    override fun toString() = "CreateUserRequest(login=$login, role=$role, clubId=$clubId)"
}