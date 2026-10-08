package ru.culture.house.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "events")
class Event(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    var club: Club,

    @Column(name = "event_date", nullable = false)
    var eventDate: LocalDate,

    @Column(name = "event_start_time")
    var eventStartTime: LocalDateTime? = null,

    @Column(name = "event_end_time")
    var eventEndTime: LocalDateTime? = null,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: EventType,

    var venue: String? = null,

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var responsibleUsers: MutableList<User> = mutableListOf(),

    @Column(name = "ticket_price")
    var ticketPrice: BigDecimal? = null,

    @Column(nullable = false)
    var revenue: BigDecimal = BigDecimal.ZERO,

    @Column(columnDefinition = "text")
    var comment: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attendance: MutableList<EventAttendance> = mutableListOf(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var disabilityAttendance: MutableList<EventDisabilityAttendance> = mutableListOf(),
) {

    @PrePersist
    @PreUpdate
    private fun normalizeBeforeSave() {
        eventStartTime?.let { eventDate = it.toLocalDate() }
    }
}