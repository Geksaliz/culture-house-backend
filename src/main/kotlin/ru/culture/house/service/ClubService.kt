package ru.culture.house.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.culture.house.controller.dto.ClubRequest
import ru.culture.house.controller.dto.ClubResponse
import ru.culture.house.controller.dto.toResponse
import ru.culture.house.exception.ConflictException
import ru.culture.house.exception.NotFoundException
import ru.culture.house.persistence.entity.Club
import ru.culture.house.persistence.repository.ClubRepository

@Service
class ClubService(
    private val clubRepository: ClubRepository,
) {

    fun create(request: ClubRequest): ClubResponse =
        clubRepository.save(
            Club(
                name = request.name.trim(),
                address = request.address.normalized(),
            )
        ).toResponse()

    @Transactional
    fun update(id: Long, request: ClubRequest): ClubResponse {
        val club = clubRepository.findById(id)
            .orElseThrow { NotFoundException("Клуб с id=$id не найден") }
        club.name = request.name.trim()
        club.address = request.address.normalized()
        return club.toResponse()
    }

    /**
     * Клуб с привязанными данными (пользователи, мероприятия, билеты, касса)
     * не удаляется: внешние ключи в схеме без каскада, это осознанная защита.
     * flush() нужен, чтобы нарушение ключа возникло здесь, а не при коммите
     * за пределами метода, где его уже нельзя превратить в 409.
     */
    @Transactional
    fun delete(id: Long) {
        if (!clubRepository.existsById(id)) {
            throw NotFoundException("Клуб с id=$id не найден")
        }
        try {
            clubRepository.deleteById(id)
            clubRepository.flush()
        } catch (e: DataIntegrityViolationException) {
            throw ConflictException(
                "Клуб нельзя удалить: с ним связаны пользователи или данные (мероприятия, билеты, касса)"
            )
        }
    }

    private fun String?.normalized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}