package ru.culture.house.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.culture.house.controller.dto.CreateUserRequest
import ru.culture.house.controller.dto.UserResponse
import ru.culture.house.controller.dto.toResponse
import ru.culture.house.exception.BadRequestException
import ru.culture.house.exception.ConflictException
import ru.culture.house.exception.NotFoundException
import ru.culture.house.persistence.entity.Club
import ru.culture.house.persistence.entity.User
import ru.culture.house.persistence.entity.UserRole
import ru.culture.house.persistence.repository.ClubRepository
import ru.culture.house.persistence.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val clubRepository: ClubRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    /**
     * Дубликат логина ловим на уровне БД (unique constraint), а не проверкой
     * existsByLogin: так нет гонки между проверкой и вставкой. К моменту save
     * клуб уже проверен, поэтому единственное, что может нарушить целостность, — логин.
     */
    @Transactional
    fun create(request: CreateUserRequest): UserResponse {
        val club = resolveClub(request)
        val user = User(
            club = club,
            login = request.login.trim(),
            passwordHash = requireNotNull(passwordEncoder.encode(request.password)),
            role = request.role,
        )
        try {
            userRepository.saveAndFlush(user)
        } catch (e: DataIntegrityViolationException) {
            throw ConflictException("Логин «${user.login}» уже занят")
        }
        return user.toResponse()
    }

    /** club_id NULL допустим только у SUPER_ADMIN — иначе нарушается изоляция клубов. */
    // TODO: вынести в валидацию
    private fun resolveClub(request: CreateUserRequest): Club? {
        val clubId = request.clubId
        if (request.role == UserRole.SUPER_ADMIN) {
            if (clubId != null) throw BadRequestException("SUPER_ADMIN не привязывается к клубу")
            return null
        }
        if (clubId == null) throw BadRequestException("Для роли ${request.role} нужно указать clubId")
        return clubRepository.findById(clubId)
            .orElseThrow { NotFoundException("Клуб с id=$clubId не найден") }
    }
}