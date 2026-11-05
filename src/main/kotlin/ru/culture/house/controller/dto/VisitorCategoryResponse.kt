package ru.culture.house.controller.dto

import ru.culture.house.persistence.entity.AgeGroup

data class VisitorCategoryResponse(
    val name: String,
    val ageGroup: AgeGroup,
)
