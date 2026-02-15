package com.dinar.myproject.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tx")
data class Tx(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val isIncome: Boolean,
    val categoryId: Long,
    val dateMillis: Long,
    val note: String?
)
