package ru.culture.house.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.culture.house.persistence.entity.DisabilityCategory

interface DisabilityCategoryRepository : JpaRepository<DisabilityCategory, Long>
