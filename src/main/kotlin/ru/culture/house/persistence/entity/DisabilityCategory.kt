package ru.culture.house.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "disability_categories")
class DisabilityCategory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(name = "discount_percent", nullable = false)
    var discountPercent: BigDecimal = BigDecimal.ZERO,
)
