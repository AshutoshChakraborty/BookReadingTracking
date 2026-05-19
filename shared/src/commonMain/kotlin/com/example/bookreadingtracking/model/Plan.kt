package com.example.bookreadingtracking.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlanStatus {
    ACTIVE, COMPLETED, ON_HOLD
}

@Serializable
data class Plan(
    val id: String,
    val title: String,
    val status: PlanStatus = PlanStatus.ACTIVE
)
