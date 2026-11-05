package ru.culture.house.controller.dto

import java.math.BigDecimal

data class DisabilityCategoryResponse(
    val id: Long,
    val name: String,
    val discountPercent: BigDecimal,
)
