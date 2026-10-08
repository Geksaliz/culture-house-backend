package ru.culture.house.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "ticket_types")
class TicketType(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    var club: Club,

    @Column(name = "nominal_value")
    var nominalValue: BigDecimal? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
)