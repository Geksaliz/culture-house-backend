package ru.culture.house.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Леджер кассовых операций: текущая касса = SUM(INCOME_EVENT + INCOME_OTHER)
 * - SUM(EXPENSE + WITHDRAWAL), с учётом ADJUSTMENT, по club_id (+ период).
 * Как и с билетами — не хранится отдельным редактируемым числом.
 *
 * INCOME_EVENT + sourceEvent != null -> создаётся/обновляется автоматически
 * при сохранении платного мероприятия (см. unique-индекс
 * uq_cash_operations_auto_income — не более одной авто-записи на мероприятие).
 */
@Entity
@Table(name = "cash_operations")
class CashOperation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    var club: Club,

    @Column(nullable = false)
    var date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: CashOperationType,

    @Column(nullable = false)
    var amount: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_event_id")
    var sourceEvent: Event? = null,

    @Column(columnDefinition = "text")
    var comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)