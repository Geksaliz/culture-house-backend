package ru.culture.house.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Леджер движения билетов данного номинала: остаток нигде не хранится
 * отдельным числом, а считается агрегацией (REPLENISHMENT + ADJUSTMENT - SALE)
 * по club_id + ticket_type_id (+ период, для отчётов).
 *
 * amount — сумма в рублях, осмысленна для SALE: quantity × nominal_value
 * для билета с номиналом либо сумма, введённая вручную, для квитанции
 * (nominal_value = NULL). events.revenue = сумма amount по SALE мероприятия.
 *
 * sourceEvent != null и type == SALE -> продажа, привязанная к мероприятию;
 * по паре (мероприятие, номинал) допускается не более одной такой записи
 * (uq_ticket_movements_auto_sale).
 */
@Entity
@Table(name = "ticket_movements")
class TicketMovement(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    var club: Club,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    var ticketType: TicketType,

    @Column(nullable = false)
    var date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    var type: TicketMovementType,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(columnDefinition = "text")
    var comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_event_id")
    var sourceEvent: Event? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)
