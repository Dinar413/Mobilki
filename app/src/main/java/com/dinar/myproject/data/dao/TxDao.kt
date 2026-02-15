package com.dinar.myproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinar.myproject.data.entities.Tx
import com.dinar.myproject.data.entities.TxWithCategory

@Dao
interface TxDao {

    @Insert
    suspend fun insert(tx: Tx): Long

    @Query("""
        SELECT 
            tx.id AS id,
            tx.userId AS userId,
            tx.amount AS amount,
            tx.isIncome AS isIncome,
            categories.name AS categoryName,
            tx.dateMillis AS dateMillis,
            tx.note AS note
        FROM tx
        INNER JOIN categories ON categories.id = tx.categoryId
        WHERE tx.userId = :userId
        ORDER BY tx.dateMillis DESC
    """)
    suspend fun listWithCategory(userId: Long): List<TxWithCategory>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM tx WHERE userId = :userId AND isIncome = 1")
    suspend fun sumIncome(userId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM tx WHERE userId = :userId AND isIncome = 0")
    suspend fun sumExpense(userId: Long): Double

    @Query("DELETE FROM tx WHERE id = :txId")
    suspend fun deleteById(txId: Long)

    @Query("DELETE FROM tx WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}
