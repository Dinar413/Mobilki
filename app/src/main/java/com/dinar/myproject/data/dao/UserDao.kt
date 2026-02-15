package com.dinar.myproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dinar.myproject.data.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun findByLogin(login: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?
}
