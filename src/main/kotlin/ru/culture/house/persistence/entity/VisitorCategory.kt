package ru.culture.house.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "visitor_categories")
class VisitorCategory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 150)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 5)
    var ageGroup: AgeGroup,
)