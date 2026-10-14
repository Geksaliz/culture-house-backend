package ru.culture.house.controller.dto

data class ClubRequest(
    val name: String,
    val address: String? = null,
)