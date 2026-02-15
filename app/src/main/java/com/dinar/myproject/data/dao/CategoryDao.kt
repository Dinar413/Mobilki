package com.dinar.myproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinar.myproject.data.entities.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories WHERE isIncome = :isIncome ORDER BY name")
    suspend fun list(isIncome: Boolean): List<Category>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)
}
