package com.dinar.myproject.data.entities

data class TxWithCategory(
    val id: Long,
    val userId: Long,
    val amount: Double,
    val isIncome: Boolean,
    val categoryName: String,
    val dateMillis: Long,
    val note: String?
)
