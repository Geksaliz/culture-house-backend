package ru.culture.house.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.culture.house.persistence.entity.User

@Repository
interface UserRepository: JpaRepository<User, Long>