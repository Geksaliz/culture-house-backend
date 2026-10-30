package ru.culture.house.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.culture.house.persistence.entity.Club

@Repository
interface ClubRepository : JpaRepository<Club, Long>