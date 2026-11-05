package ru.culture.house.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.culture.house.persistence.entity.Event
import java.time.LocalDate

interface EventRepository : JpaRepository<Event, Long> {
    @Query("""
        select e from Event e where e.club.id = :clubId
        and e.eventDate >= :dateFrom and e.eventDate <= :dateTo
        order by e.eventDate, e.id
    """)
    fun findInPeriod(clubId: Long, dateFrom: LocalDate, dateTo: LocalDate): List<Event>
}
