package ru.culture.house.persistence.entity

enum class UserRole { SUPER_ADMIN, ADMIN, EMPLOYEE }

enum class EventType { PAID, FREE }

enum class AgeGroup { CHILD, TEEN, ADULT }

enum class TicketMovementType { REPLENISHMENT, SALE, ADJUSTMENT }

enum class CashOperationType { INCOME_EVENT, INCOME_OTHER, EXPENSE, WITHDRAWAL, ADJUSTMENT }