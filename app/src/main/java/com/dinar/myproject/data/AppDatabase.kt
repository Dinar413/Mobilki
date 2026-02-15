package com.dinar.myproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dinar.myproject.data.dao.CategoryDao
import com.dinar.myproject.data.dao.TxDao
import com.dinar.myproject.data.dao.UserDao
import com.dinar.myproject.data.entities.Category
import com.dinar.myproject.data.entities.Tx
import com.dinar.myproject.data.entities.User

@Database(
    entities = [User::class, Category::class, Tx::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun txDao(): TxDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_db"
                ).build().also { INSTANCE = it }
            }
    }
}
