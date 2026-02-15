package com.dinar.myproject.data.repo

import com.dinar.myproject.data.AppDatabase
import com.dinar.myproject.data.entities.Category
import com.dinar.myproject.data.entities.Tx
import com.dinar.myproject.data.entities.TxWithCategory


class FinanceRepository(private val db: AppDatabase) {

    suspend fun getUserRole(userId: Long): String {
        return db.userDao().findById(userId)?.role ?: "MANAGER"
    }

    suspend fun totals(userId: Long): Triple<Double, Double, Double> {
        val income = db.txDao().sumIncome(userId)
        val expense = db.txDao().sumExpense(userId)
        val balance = income - expense
        return Triple(balance, income, expense)
    }

    suspend fun listTxWithCategory(userId: Long): List<TxWithCategory> =
        db.txDao().listWithCategory(userId)

    suspend fun deleteTx(txId: Long) {
        db.txDao().deleteById(txId)
    }

    suspend fun deleteAllTx(userId: Long) {
        db.txDao().deleteAllByUser(userId)
    }

    suspend fun categories(isIncome: Boolean): List<Category> = db.categoryDao().list(isIncome)

    suspend fun addCategory(name: String, isIncome: Boolean) {
        db.categoryDao().insert(Category(name = name.trim(), isIncome = isIncome))
    }

    suspend fun deleteCategory(id: Long) {
        db.categoryDao().delete(id)
    }

    suspend fun addTx(
        userId: Long,
        amount: Double,
        isIncome: Boolean,
        categoryId: Long,
        note: String?
    ) {
        db.txDao().insert(
            Tx(
                userId = userId,
                amount = amount,
                isIncome = isIncome,
                categoryId = categoryId,
                dateMillis = System.currentTimeMillis(),
                note = note?.trim()
            )
        )
    }
}
